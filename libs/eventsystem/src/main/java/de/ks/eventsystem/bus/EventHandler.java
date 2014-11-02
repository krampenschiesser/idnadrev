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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.LoggingUncaughtExceptionHandler;
import de.ks.reflection.ReflectionUtil;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 *
 */
class EventHandler {
  private static final Logger log = LoggerFactory.getLogger(EventHandler.class);
  protected final WeakReference<Object> target;
  protected final Method method;
  protected final Integer priority;
  protected final HandlingThread handlingThread;
  protected final ExecutorService service;
  protected final JavaFXExecutorService fxExecutor;

  protected EventHandler(ExecutorService service, JavaFXExecutorService fxExecutor, Object target, Method method) {
    this.target = new WeakReference<>(target);
    this.method = method;
    if (method.isAnnotationPresent(Priority.class)) {
      Priority annotation = method.getAnnotation(Priority.class);
      priority = annotation.value();
    } else {
      priority = Integer.MAX_VALUE;
    }
    if (method.isAnnotationPresent(Threading.class)) {
      handlingThread = method.getAnnotation(Threading.class).value();
    } else {
      handlingThread = HandlingThread.Sync;
    }
    if (service == null) {
      ThreadFactory threadFactory = new ThreadFactoryBuilder()//
        .setDaemon(true)//
        .setNameFormat("Eventsystem-%d")//
        .setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler())//
        .build();
      this.service = Executors.newCachedThreadPool(threadFactory);
    } else {
      this.service = service;
    }
    if (fxExecutor == null) {
      this.fxExecutor = new JavaFXExecutorService();
    } else {
      this.fxExecutor = fxExecutor;
    }
  }

  public boolean handleEvent(Object event, boolean wait) {
    Object targetInstance = target.get();
    if (targetInstance != null) {
      Object retval = null;
      switch (this.handlingThread) {
        case Sync:
          retval = ReflectionUtil.invokeMethod(method, targetInstance, event);
          break;
        case Async:
          executeAsync(event, targetInstance, wait);
          break;
        case JavaFX:
          retval = handleInJavaFXThread(event, targetInstance, wait);
          break;
      }

      if (retval instanceof Boolean) {
        return (Boolean) retval;
      } else if (retval != null && retval.getClass().isPrimitive() && Boolean.TYPE.equals(retval.getClass())) {
        return (boolean) retval;
      }
    }
    return false;
  }

  protected void executeAsync(Object event, Object targetInstance, boolean wait) {
    Future<?> future = service.submit((Runnable) () -> ReflectionUtil.invokeMethod(method, targetInstance, event));
    if (wait) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("Could not execute event asynchronously ", e);
      }
    }
  }

  protected Object handleInJavaFXThread(Object event, Object targetInstance, boolean wait) {
    if (Platform.isFxApplicationThread()) {
      ReflectionUtil.invokeMethod(method, targetInstance, event);
    } else {
      FutureTask<Class<Void>> task = new FutureTask<>(() -> ReflectionUtil.invokeMethod(method, targetInstance, event), Void.class);
      fxExecutor.submit(task);
      if (wait) {
        try {
          task.get();
        } catch (InterruptedException | ExecutionException e) {
          log.error("Could not execute event in JavaFX thread", e);
        }
      }
    }
    return null;
  }
}
