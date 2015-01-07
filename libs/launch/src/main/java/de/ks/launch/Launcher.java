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
package de.ks.launch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.SubclassInstantiator;
import de.ks.preload.LaunchListener;
import de.ks.preload.LaunchListenerAdapter;
import de.ks.preload.PreloaderApplication;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Launcher {
  public static final Launcher instance = new Launcher(true);

  private static final Logger log = LoggerFactory.getLogger(Launcher.class);
  public static final String SERVICE_PACKAGES = "service.packages";
  public static final String SERVICE_PROPERTIES_FILENAME = "service.properties";
  public static final String PACKAGE_SEPARATOR = ",";

  private final List<Service> services = new ArrayList<>();
  private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("launcher-%d").build());
  private final List<Throwable> startupExceptions = Collections.synchronizedList(new ArrayList<>());
  private final SubclassInstantiator instantiator;

  private volatile CountDownLatch latch;
  private volatile LaunchListener launchListener = new LaunchListenerAdapter();
  private Class<? extends PreloaderApplication> preloader;
  private Future<?> preloaderFuture;
  private PreloaderApplication preloaderInstance;
  private final CountDownLatch preloaderLatch = new CountDownLatch(1);

  protected Launcher(boolean excludeTestResources) {
    instantiator = new SubclassInstantiator(executorService, getClass().getPackage(), SERVICE_PROPERTIES_FILENAME, SERVICE_PACKAGES, PACKAGE_SEPARATOR);
    instantiator.setExcludeTestResources(excludeTestResources);
  }

  public List<Service> discoverServices() {
    List<Service> services = instantiator.instantiateSubclasses(Service.class);
    services.sort((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()));
    return services;
  }

  public List<Service> getServices() {
    if (services.isEmpty()) {
      services.addAll(discoverServices());
    }
    return services;
  }

  public <S extends Service> void removeService(Class<S> clazz) {
    S service = getService(clazz);
    services.remove(service);
  }

  @SuppressWarnings("unchecked")
  public <S extends Service> S getService(Class<S> clazz) {
    List<Service> collect = getServices().stream().filter((s) -> s.getClass().equals(clazz)).collect(Collectors.toList());
    if (collect.isEmpty()) {
      return null;
    } else {
      return (S) collect.get(0);
    }
  }

  @SuppressWarnings("unchecked")
  public <S extends Service> S getService(String name) {
    List<Service> collect = getServices().stream().filter((s) -> s.getName().equals(name)).collect(Collectors.toList());
    if (collect.isEmpty()) {
      return null;
    } else {
      return (S) collect.get(0);
    }
  }

  public TreeMap<Integer, List<Service>> getServiceWaves() {
    TreeMap<Integer, List<Service>> retval = new TreeMap<>();
    getServices().forEach((service) -> {
      int priority = service.getPriority();
      retval.putIfAbsent(priority, new ArrayList<>());
      retval.get(priority).add(service);
    });
    return retval;
  }

  public void startAll(String... args) {
    if (preloader != null) {
      startPreloader();
    }
    TreeMap<Integer, List<Service>> waves = getServiceWaves();
    launchListener.totalWaves(waves.keySet().size());
    launchListener.wavePriorities(waves.keySet());
    latch = new CountDownLatch(waves.keySet().size());
    Iterator<Integer> iter = waves.keySet().iterator();
    startWave(iter, waves, args);
    log.info("Launching done!");
  }

  private void startWave(Iterator<Integer> iter, TreeMap<Integer, List<Service>> waves, String[] args) {
    if (!iter.hasNext()) {
      return;
    }
    Integer prio = iter.next();
    launchListener.waveStarted(prio);
    log.info("Starting services with prio {}", prio);
    List<CompletableFuture<Void>> waveFutures = waves.get(prio).stream()//
            .map((s) -> {
              return CompletableFuture.supplyAsync(() -> {
                s.initialize(this, executorService, args);
                return s.start();
              }, executorService)//
                      .thenAccept((service) -> log.info("Successfully started service {}", service.getName()));
            }).collect(Collectors.toList());

    CompletableFuture<Void> allOf = CompletableFuture.allOf(waveFutures.toArray(new CompletableFuture[waveFutures.size()]));
    allOf.thenRun(() -> log.info("Started services with prio {}", prio))//
            .thenRun(() -> latch.countDown())//
            .thenRun(() -> launchListener.waveFinished(prio))//
            .thenRun(() -> startWave(iter, waves, args))//
            .exceptionally((t) -> {
              while (latch.getCount() > 0) {
                latch.countDown();
              }
              startupExceptions.add(t);
              launchListener.failure(t.toString());
              //throw new RuntimeException(t);
              return null;
            });
  }

  public void awaitStart() {
    try {
      latch.await();
      if (!startupExceptions.isEmpty()) {
        RuntimeException runtimeException = new RuntimeException("Startup failed");
        startupExceptions.forEach((t) -> {
          log.error("Failed startup.", t);
          runtimeException.addSuppressed(t);
        });
        throw runtimeException;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isStarted() {
    for (Service service : getServices()) {
      if (!service.isRunning()) {
        return false;
      }
    }
    return true;
  }

  public void stopAll() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      log.error("Could not await latch.", e);
    }
    TreeMap<Integer, List<Service>> waves = getServiceWaves();
    latch = new CountDownLatch(waves.keySet().size());
    Iterator<Integer> iter = waves.descendingKeySet().iterator();
    stopWave(iter, waves);
  }

  private void stopWave(Iterator<Integer> iter, TreeMap<Integer, List<Service>> waves) {
    if (!iter.hasNext()) {
      return;
    }
    Integer prio = iter.next();
    log.info("Stopping services with prio {}", prio);
    List<CompletableFuture<Void>> waveFutures = waves.get(prio).stream()//
            .map((s) -> {
              if (s.isStopped()) {
                return CompletableFuture.<Void>completedFuture(null);
              } else {
                return CompletableFuture.supplyAsync(() -> s.stop(), executorService)//
                        .thenAccept((service) -> log.info("Successfully stopped service {}", service.getName()));
              }
            }).collect(Collectors.toList());

    CompletableFuture<Void> allOf = CompletableFuture.allOf(waveFutures.toArray(new CompletableFuture[waveFutures.size()]));
    allOf.thenRun(() -> log.info("Stopped services with prio {}", prio))//
            .thenRun(() -> latch.countDown())//
            .thenRun(() -> stopWave(iter, waves))//
            .exceptionally((t) -> {
              log.info("Failed to stop services", t);
              return null;
            });
  }

  public void awaitStop() {
    if (latch != null) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    waitForPreloader();
  }

  public void waitForPreloader() {
    if (preloaderFuture != null) {
      try {
        preloaderFuture.get();
      } catch (InterruptedException e) {
        //ok
      } catch (ExecutionException e) {
        log.error("Error from preloader ", e);
      }
    }
  }

  public void startPreloader() {
    preloaderFuture = executorService.submit(() -> Application.launch(preloader));
    try {
      preloaderLatch.await();
    } catch (InterruptedException e) {
      //
    }
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public void setPreloader(Class<? extends PreloaderApplication> preloader) {
    this.preloader = preloader;
  }

  public Class<? extends PreloaderApplication> getPreloader() {
    return preloader;
  }

  public void setLaunchListener(LaunchListener launchListener) {
    this.launchListener = launchListener;
  }

  public LaunchListener getLaunchListener() {
    return launchListener;
  }

  public void setPreloaderInstance(PreloaderApplication preloaderInstance) {
    this.preloaderInstance = preloaderInstance;
    preloaderLatch.countDown();
  }

  public PreloaderApplication getPreloaderInstance() {
    return preloaderInstance;
  }
}
