/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
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
package de.ks;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CDIRunner extends BlockJUnit4ClassRunner {
  private static final Logger log = LoggerFactory.getLogger(CDIRunner.class);

  protected static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
    Thread t = new Thread(r);
    t.setDaemon(true);
    return t;
  });
  private static final CountDownLatch barrier = new CountDownLatch(1);
  private static final CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();

  public CDIRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  public void run(RunNotifier notifier) {
    if (barrier.getCount() > 0) {
      start();
      await();
    }
    notifier.addListener(new RunListener() {
      @Override
      public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        log.info("@Test:{}.{}", description.getClassName(), description.getMethodName());
      }
    });
    super.run(notifier);
  }

  @Override
  protected Object createTest() throws Exception {
    Instance<?> select = CDI.current().select(getTestClass().getJavaClass());
    if (select.isUnsatisfied()) {
      return getTestClass().getJavaClass().newInstance();
    } else {
      return select.get();
    }
  }

  protected void start() {
    executor.execute(() -> {
      try {
        cdiContainer.boot();
      } finally {
        barrier.countDown();
      }
    });
  }

  protected void await() {
    try {
      if (!barrier.await(5, TimeUnit.SECONDS)) {
        stop();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  protected void stop() {
    cdiContainer.shutdown();
  }
}
