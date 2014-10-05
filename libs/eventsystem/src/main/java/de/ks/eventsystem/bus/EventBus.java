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

package de.ks.eventsystem.bus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Primitives;
import de.ks.executor.JavaFXExecutorService;
import de.ks.reflection.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.CDI;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 *
 */
@Vetoed
public class EventBus {
  /**
   * Used to synchronous execute all events, mainly in tests
   */
  public static boolean alwaysWait = false;
  private static final Logger log = LoggerFactory.getLogger(EventBus.class);
  protected final ArrayListMultimap<Class<?>, EventHandler> handlers = ArrayListMultimap.create();
  protected final HandlerComparator handlerComparator = new HandlerComparator();
  protected final ReadWriteLock lock = new ReentrantReadWriteLock(true);
  protected ExecutorService executorService = null;
  protected JavaFXExecutorService javaFXExecutorService = null;

  public EventBus register(Object handler) {
    @SuppressWarnings("unchecked") List<Method> methods = ReflectionUtil.getAllMethods(handler.getClass(),//
            (Method m) -> m.isAnnotationPresent(Subscribe.class),          //
            (Method m) -> m.getParameters().length == 1                    //
    );

    lock.writeLock().lock();
    try {
      for (Method method : methods) {
        Class<?> eventType = getEventType(method);
        handlers.put(eventType, new EventHandler(executorService, javaFXExecutorService, handler, method));
        List<EventHandler> eventHandlers = handlers.get(eventType);//could be optimized by not sorting an event type multiple times
        Collections.sort(eventHandlers, getHandlerComparator());
      }
    } finally {
      lock.writeLock().unlock();
    }
    return this;
  }

  public EventBus unregister(Object handler) {
    @SuppressWarnings("unchecked") List<Method> methods = ReflectionUtil.getAllMethods(handler.getClass(),//
            (Method m) -> m.isAnnotationPresent(Subscribe.class),          //
            (Method m) -> m.getParameters().length == 1                    //
    );

    Set<Class<?>> classes = methods.stream().map(this::getEventType).collect(Collectors.toSet());

    for (Class<?> clazz : classes) {
      List<EventHandler> eventHandlers = handlers.get(clazz);
      removeSpecificHandler(handler, eventHandlers);
    }
    return this;
  }

  private void removeSpecificHandler(Object specificHandler, List<EventHandler> eventHandlers) {
    lock.writeLock().lock();
    try {

      for (Iterator<EventHandler> iterator = eventHandlers.iterator(); iterator.hasNext(); ) {
        EventHandler next = iterator.next();
        Object instance = next.target.get();
        if (instance != null) {
          if (instance == specificHandler) {
            iterator.remove();
          }
        } else {
          iterator.remove();
        }
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public EventBus post(Object event) {
    post(event, EventTarget.Default, alwaysWait);
    return this;
  }

  public EventBus post(Object event, EventTarget target) {
    post(event, target, alwaysWait);
    return this;
  }

  public EventBus postAndWait(Object event) {
    post(event, EventTarget.Default, true);
    return this;
  }

  public EventBus postAndWait(Object event, EventTarget target) {
    post(event, target, true);
    return this;
  }

  protected void post(Object event, EventTarget target, boolean wait) {

    if (target == EventTarget.Default) {
      postToEventHandlers(event, wait);
    } else {
      CDI.current().getBeanManager().fireEvent(event);
    }
  }

  private void postToEventHandlers(Object event, boolean wait) {
    List<Class<?>> hierarchy = ReflectionUtil.getClassHierarchy(event.getClass(), false);
    LinkedList<EventExecution> executions = new LinkedList<>();
    lock.readLock().lock();
    try {
      for (Class<?> eventType : hierarchy) {
        List<EventHandler> eventHandlers = handlers.get(eventType);
        for (EventHandler eventHandler : eventHandlers) {
          executions.add(new EventExecution(event, eventHandler));
        }
      }
    } finally {
      lock.readLock().unlock();
    }

    for (EventExecution execution : executions) {
      boolean consumed = execution.handler.handleEvent(event, wait);
      if (consumed) {
        break;
      }
    }

    boolean noHandlerFound = executions.isEmpty();
    boolean isDeadEvent = event instanceof DeadEvent;

    if (noHandlerFound && !isDeadEvent) {
      postToEventHandlers(new DeadEvent(this, event), false);
    } else if (noHandlerFound) {
      log.warn("Could not find dead event handler.");
    }
  }

  protected HandlerComparator getHandlerComparator() {
    return handlerComparator;
  }

  protected Class<?> getEventType(Method method) {
    Class<?> type = method.getParameterTypes()[0];
    if (type.isPrimitive()) {
      return Primitives.wrap(type);
    }
    return type;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public JavaFXExecutorService getJavaFXExecutorService() {
    return javaFXExecutorService;
  }

  public void setJavaFXExecutorService(JavaFXExecutorService javaFXExecutorService) {
    this.javaFXExecutorService = javaFXExecutorService;
  }
}
