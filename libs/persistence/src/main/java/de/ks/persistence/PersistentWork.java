/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.persistence;

import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.persistence.transaction.SimpleTransaction;
import de.ks.persistence.transaction.TransactionProvider;
import de.ks.persistence.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Handles transactions and {@link EntityManager} closing.
 * Usually used as an anonymous class.
 */
public class PersistentWork {
  private static final Logger log = LoggerFactory.getLogger(PersistentWork.class);

  public static <R> R wrap(Supplier<R> supplier) {
    return PersistentWork.read(em -> supplier.get());
  }

  public static void wrap(Runnable runnable) {
    PersistentWork.run(em -> runnable.run());
  }

  public static CompletableFuture<Void> runAsync(Consumer<EntityManager> consumer) {
    ExecutorService executorService = CDI.current().select(ExecutorService.class).get();
    return runAsync(consumer, executorService);
  }

  public static CompletableFuture<Void> runAsync(Consumer<EntityManager> consumer, ExecutorService executorService) {
    CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> run(consumer), executorService);
    return completableFuture;
  }

  public static <T> CompletableFuture<T> readAsync(Function<EntityManager, T> function) {
    ExecutorService executorService = CDI.current().select(ExecutorService.class).get();
    return readAsync(function, executorService);
  }

  public static <T> CompletableFuture<T> readAsync(Function<EntityManager, T> function, ExecutorService executorService) {
    CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(() -> read(function), executorService);
    return completableFuture;
  }

  public static void run(Consumer<EntityManager> consumer) {
    new PersistentWork(consumer).run();
  }

  public static <T> T read(Function<EntityManager, T> function) {
    return new PersistentWork(function).run();
  }

  public static <T> T reload(T instance) {
    return reload(instance, null);
  }

  public static <T> T reload(T instance, Consumer<T> resultWalker) {

    return read(em -> {
      if (instance instanceof AbstractPersistentObject) {
        AbstractPersistentObject apo = (AbstractPersistentObject) instance;
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) instance.getClass();
        T reloaded = em.<T>find(clazz, apo.getId());
        if (reloaded == null) {
          return instance;
        }
        if (resultWalker != null) {
          resultWalker.accept(reloaded);
        }
        return (T) reloaded;
      } else {
        return instance;
      }
    });
  }

  @SuppressWarnings("unchecked")
  public static <T> void merge(T... t) {
    merge(Arrays.asList(t));
  }

  public static <T> void merge(List<T> all) {
    run((em) -> all.forEach(em::merge));
  }

  public static <T> void persist(T... t) {
    persist(Arrays.asList(t));
  }

  public static <T> void persist(List<T> all) {
    run((em) -> all.forEach(t -> {
      em.persist(t);
    }));
  }

  public static <T> T byId(Class<T> clazz, Long id) {
    return read((em) -> em.find(clazz, id));
  }

  public static void deleteAllOf(Class<?>... classes) {
    run((em) -> {
      for (Class<?> clazz : classes) {
        int deletedLines = em.createQuery("delete from " + clazz.getName()).executeUpdate();
        log.debug("Deleted {} from {}", deletedLines, clazz.getSimpleName());
      }
    });
  }

  public static <T extends NamedPersistentObject<T>> T forName(Class<T> clazz, String name) {
    List<T> results = forName(clazz, name, false);
    if (results.size() != 1) {
      return null;
    } else {
      return results.get(0);
    }
  }

  public static <T extends NamedPersistentObject<T>> List<T> forNameLike(Class<T> clazz, String name) {
    return forName(clazz, name, true);
  }

  private static <T extends NamedPersistentObject<T>> List<T> forName(Class<T> clazz, String name, boolean like) {
    List<T> results = from(clazz, (Root<T> root, CriteriaQuery<T> query, CriteriaBuilder builder) -> {
      Predicate restriction;
      if (like) {
        restriction = builder.like(builder.lower(root.get("name")), name.toLowerCase() + "%");
      } else {
        restriction = builder.equal(builder.lower(root.get("name")), name.toLowerCase());
      }
      query.where(restriction);
    }, null);
    return results;
  }

  public static <T> List<T> from(Class<T> clazz) {
    return from(clazz, null, null);
  }

  public static <T> List<T> from(Class<T> clazz, Consumer<T> resultWalker) {
    return from(clazz, null, resultWalker);
  }

  public static <T> List<T> from(Class<T> clazz, QueryConsumer<T> consumer, Consumer<T> resultWalker) {
    return read((em) -> {
      CriteriaQuery<T> query = (CriteriaQuery<T>) em.getCriteriaBuilder().createQuery(clazz);

      Root<T> root = query.from(clazz);
      query.select(root);

      if (consumer != null) {
        consumer.accept(root, query, em.getCriteriaBuilder());
      }

      List<T> resultList = em.createQuery(query).getResultList();
      if (resultWalker != null) {
        resultList.forEach(resultWalker);
      }
      return resultList;
    });
  }

  protected static final ThreadLocal<EntityManager> localEM = new ThreadLocal<>();
  protected Consumer<EntityManager> consumer;
  protected Function<EntityManager, ?> function;

  private PersistentWork() {
  }

  private PersistentWork(Consumer<EntityManager> consumer) {
    this();
    this.consumer = consumer;
  }

  private PersistentWork(Function<EntityManager, ?> function) {
    this();
    this.function = function;
  }

  @SuppressWarnings("unchecked")
  protected <T> T run() {
    Optional<SimpleTransaction> txPesent = TransactionProvider.instance.getCurrentTransaction();
    if (localEM.get() != null && txPesent.isPresent()) {
      Object retval = execute();
      return (T) retval;
    } else {
      localEM.set(CDI.current().select(EntityManager.class).get());
      Object run = Transactional.withNewTransaction(() -> {
        EntityManager em = localEM.get();
        TransactionProvider.instance.registerEntityManager(em);
        return execute();
      });
      return (T) run;
    }
  }

  protected Object execute() {
    if (consumer != null) {
      consumer.accept(localEM.get());
      return null;
    } else if (function != null) {
      return function.apply(localEM.get());
    }
    return null;
  }
}
