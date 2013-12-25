package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.collect.ArrayListMultimap;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Primitives;
import de.ks.reflection.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.CDI;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

  public EventBus register(Object handler) {
    @SuppressWarnings("unchecked") List<Method> methods = ReflectionUtil.getAllMethods(handler.getClass(),//
            (Method m) -> m.isAnnotationPresent(Subscribe.class),          //
            (Method m) -> m.getParameters().length == 1                    //
    );

    lock.writeLock().lock();
    try {
      for (Method method : methods) {
        Class<?> eventType = getEventType(method);
        handlers.put(eventType, new EventHandler(handler, method));
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

    HashSet<Class<?>> classes = new HashSet<>();
    for (Method method : methods) {
      classes.add(getEventType(method));
    }

    for (Class<?> clazz : classes) {
      List<EventHandler> eventHandlers = handlers.get(clazz);

      lock.writeLock().lock();
      try {

        for (Iterator<EventHandler> iterator = eventHandlers.iterator(); iterator.hasNext(); ) {
          EventHandler next = iterator.next();
          if (next.isValid()) {
            Object instance = next.target.get();
            if (instance == handler) {
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
    return this;
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
    List<Class<?>> hierarchy = ReflectionUtil.getClassHierarchy(event.getClass());
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
    } else if (noHandlerFound && isDeadEvent) {
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
}
