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

package de.ks.activity.context;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class ActivityContext implements Context {
  private static final Logger log = LoggerFactory.getLogger(ActivityContext.class);
  protected final ConcurrentHashMap<String, ActivityHolder> activities = new ConcurrentHashMap<>();
  protected volatile String currentActivity = null;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
  private final BeanManager beanManager;

  public static void start(String id) {
    ActivityContext context = (ActivityContext) CDI.current().getBeanManager().getContext(ActivityScoped.class);
    context.startActivity(id);
  }

  public static void stop(String id) {
    ActivityContext context = (ActivityContext) CDI.current().getBeanManager().getContext(ActivityScoped.class);
    context.stopActivity(id);
  }

  public static void cleanup(String id) {
    ActivityContext context = (ActivityContext) CDI.current().getBeanManager().getContext(ActivityScoped.class);
    context.cleanupSingleActivity(id);
  }

  public static void stopAll() {
    ActivityContext context = (ActivityContext) CDI.current().getBeanManager().getContext(ActivityScoped.class);
    context.cleanupAllActivities();
  }

  public ActivityContext(BeanManager mgr) {
    beanManager = mgr;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return ActivityScoped.class;
  }

  @Override
  public <T> T get(Contextual<T> contextual) {
    if (contextual instanceof Bean) {
      Bean bean = (Bean) contextual;

      Pair<String, Class<?>> key = getKey(bean);
      lock.readLock().lock();
      try {
        StoredBean storedBean = activities.get(key.getLeft()).getStoredBean(key.getRight());
        if (storedBean != null) {
          return storedBean.getInstance();
        }
      } finally {
        lock.readLock().unlock();
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
    if (contextual instanceof Bean) {
      Bean bean = (Bean) contextual;
      Pair<String, Class<?>> key = getKey(bean);

      lock.writeLock().lock();
      try {
        Object o = bean.create(creationalContext);
        StoredBean storedBean = new StoredBean(bean, creationalContext, o);
        activities.get(key.getLeft()).put(key.getRight(), storedBean);
        return (T) o;
      } finally {
        lock.writeLock().unlock();
      }
    }
    return null;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  protected Pair<String, Class<?>> getKey(Bean<?> bean) {
    Class<?> beanClass = bean.getBeanClass();
    Annotation annotation = beanClass.getAnnotation(ActivityScoped.class);

    if (annotation == null) {//might be a producer method, only warn

      String msg = "Unable to retrieve " + ActivityScoped.class.getName() + " from " + beanClass;
      if (bean.getClass().getName().contains("Producer")) {
        log.trace(msg);
      } else {
        log.warn(msg);
      }
    }
    if (currentActivity == null) {
      throw new IllegalStateException("No activity currently active!");
    }
    return Pair.of(currentActivity, beanClass);
  }

  public void cleanupSingleActivity(String id) {
    lock.writeLock().lock();
    try {
      ActivityHolder activityHolder = activities.remove(id);
      log.debug("Cleanup activity {}", activityHolder.getId());
      Set<Map.Entry<Class<?>, StoredBean>> entries = activityHolder.objectStore.entrySet();

      for (Iterator<Map.Entry<Class<?>, StoredBean>> iterator = entries.iterator(); iterator.hasNext(); ) {
        Map.Entry<Class<?>, StoredBean> next = iterator.next();
        next.getValue().destroy();
        iterator.remove();
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void cleanupSingleBean(Class<?> clazz) {
    lock.writeLock().lock();
    try {
      ActivityHolder activityHolder = activities.get(getCurrentActivity());

      StoredBean storedBean = activityHolder.objectStore.get(clazz);
      if (storedBean != null) {
        storedBean.destroy();
        activityHolder.objectStore.remove(clazz);
        log.debug("Cleaned up bean {} of activity {}", clazz.getName(), activityHolder.getId());
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public ActivityHolder startActivity(String id) {
    lock.writeLock().lock();
    try {
      currentActivity = id;
      if (activities.containsKey(id)) {
        log.debug("Resuming activity {}", id);
        return activities.get(id);
      } else {
        ActivityHolder holder = new ActivityHolder(id);
        this.activities.put(id, holder);

        log.debug("Started activity {}", holder.getId());
        return holder;
      }

    } finally {
      lock.writeLock().unlock();
    }
  }

  public void stopActivity(String id) {
    lock.writeLock().lock();
    try {
      ActivityHolder activityHolder = activities.get(id);
      if (activityHolder == null) {
        log.warn("Activity {} is already stopped", id);
        return;
      }
      int count = activityHolder.getCount().decrementAndGet();
      if (count == 0) {
        cleanupSingleActivity(id);
        currentActivity = null;
        log.debug("Stopped activity {}", activityHolder.getId());
      } else {
        log.debug("Don't stop activity {} because of {} holders.", activityHolder.getId(), count);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void cleanupAllActivities() {
    log.debug("Cleanup all activities.");
    for (String id : activities.keySet()) {
      ActivityHolder activityHolder = activities.get(id);
      if (activityHolder == null) {
        log.error("No activity active in thread {}", Thread.currentThread().getName());
        throw new IllegalStateException("No activity active in thread " + Thread.currentThread().getName());
      }
      if (multipleThreadsActive(activityHolder)) {
        log.warn("There are still {} other threads holding a reference to this activity, cleanup not allowed", activityHolder.getCount().get() - 1);
      }
      waitForOtherThreads(activityHolder);
      cleanupSingleActivity(id);
    }
    lock.writeLock().lock();
    try {
      this.activities.clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void waitForOtherThreads(ActivityHolder activityHolder) {
    while (multipleThreadsActive(activityHolder)) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        log.error("Interrupted", e);
      }
    }
  }

  private boolean multipleThreadsActive(ActivityHolder activityHolder) {
    return activityHolder.getCount().get() > 1;
  }

  public ActivityHolder getHolder() {
    if (currentActivity == null) {
      throw new RuntimeException("No activity active in current thread!");
    }
    return activities.get(currentActivity);
  }

  public String getCurrentActivity() {
    return currentActivity;
  }

  public boolean hasCurrentActivity() {
    return currentActivity != null;
  }
}
