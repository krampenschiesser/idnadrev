package de.ks.executor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class ExecutorServiceTest {
  private static final Logger log = LogManager.getLogger(ExecutorServiceTest.class);

  ThreadLocal<String> threadLocal = new ThreadLocal<>();
  protected String value;

  @Test
  public void testThreadPropagation() throws Exception {
    ThreadCallBoundValue threadCallBoundValue = new ThreadCallBoundValue() {
      String currentValue = null;

      @Override
      public void initializeInCallerThread() {
        log.debug("initializing");
        currentValue = threadLocal.get();
      }

      @Override
      public void doBeforeCallInTargetThread() {
        if (threadLocal.get() != null) {
          throw new RuntimeException("ThreadLocal not set to null again!");
        }
        log.debug("setting value");
        threadLocal.set(currentValue);
      }

      @Override
      public void doAfterCallInTargetThread() {
        log.debug("reset");
        threadLocal.set(null);
      }

      @Override
      public ThreadCallBoundValue clone() {
        try {
          ThreadCallBoundValue clone = (ThreadCallBoundValue) super.clone();
          return clone;
        } catch (CloneNotSupportedException e) {
          throw new InternalError("could not clone");
        }
      }
    };
    ExecutorService.instance.getPropagations().register(threadCallBoundValue);

    threadLocal.set("Hello Sauerland!");
    for (int i = 0; i < 50; i++) {
      Future<?> future = ExecutorService.instance.submit((Runnable) () -> value = threadLocal.get());
      future.get();
    }
    assertEquals("Hello Sauerland!", value);
  }
}
