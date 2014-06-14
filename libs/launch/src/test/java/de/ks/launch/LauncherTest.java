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

import com.dummy.other.ServiceB;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

public class LauncherTest {
  private static final Logger log = LoggerFactory.getLogger(LauncherTest.class);
  private Launcher launcher;

  @Before
  public void setUp() throws Exception {
    launcher = new Launcher(false);
  }

  @Test
  public void testDiscoverServices() throws Exception {
    List<Service> services = launcher.discoverServices();
    assertEquals(2, services.size());
    assertTrue(ServiceB.class.isInstance(services.get(1)));
    assertTrue(ServiceA.class.isInstance(services.get(0)));
  }

  @Test
  public void testStartServices() throws Exception {
    launcher.startAll();
    Thread.sleep(50);
    TestService serviceA = (TestService) launcher.getServices().get(0);
    TestService serviceB = (TestService) launcher.getServices().get(1);
    assertEquals(ServiceRuntimeState.STARTING, serviceA.getState());
    assertEquals(ServiceRuntimeState.STOPPED, serviceB.getState());
    serviceA.await();
    Thread.sleep(100);
    assertEquals(ServiceRuntimeState.RUNNING, serviceA.getState());
    assertEquals(ServiceRuntimeState.STARTING, serviceB.getState());
    serviceB.await();
    Thread.sleep(100);
    assertEquals(ServiceRuntimeState.RUNNING, serviceA.getState());
    assertEquals(ServiceRuntimeState.RUNNING, serviceB.getState());
  }

  @Test
  public void testAwaitStart() throws Exception {
    launcher.startAll();
    TestService serviceA = (TestService) launcher.getServices().get(0);
    TestService serviceB = (TestService) launcher.getServices().get(1);
    ForkJoinPool.commonPool().submit(() -> {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      serviceA.await();
      serviceB.await();
    });
    long start = System.currentTimeMillis();
    launcher.awaitStart();
    assertEquals(ServiceRuntimeState.RUNNING, serviceB.getState());
    assertEquals(ServiceRuntimeState.RUNNING, serviceA.getState());
    long end = System.currentTimeMillis();

    long took = end - start;
    log.info("Took {}ms", took);
    assertTrue(took > 50);
  }

  @Test
  public void testStartFailure() throws Exception {
    TestService serviceA = (TestService) launcher.getServices().get(0);
    TestService serviceB = (TestService) launcher.getServices().get(1);
    serviceA.fail();
    try {
      launcher.startAll();
      serviceA.await();
      launcher.awaitStart();
      fail("awaitStart() should rethrow exception");
    } catch (RuntimeException e) {
      assertEquals(ServiceRuntimeState.STOPPED, serviceA.getState());
      assertEquals(ServiceRuntimeState.STOPPED, serviceB.getState());
    }
  }

  @Test
  public void testStopping() throws Exception {
    TestService serviceA = (TestService) launcher.getServices().get(0);
    TestService serviceB = (TestService) launcher.getServices().get(1);
    launcher.startAll();
    serviceA.await();
    serviceB.await();
    Thread.sleep(100);
    launcher.awaitStart();


    assertEquals(ServiceRuntimeState.RUNNING, serviceA.getState());
    assertEquals(ServiceRuntimeState.RUNNING, serviceB.getState());

    launcher.stopAll();
    assertEquals(ServiceRuntimeState.RUNNING, serviceA.getState());
    assertEquals(ServiceRuntimeState.STOPPING, serviceB.getState());

    serviceB.await();
    Thread.sleep(100);
    assertEquals(ServiceRuntimeState.STOPPING, serviceA.getState());
    assertEquals(ServiceRuntimeState.STOPPED, serviceB.getState());

    serviceA.await();
    Thread.sleep(100);
    assertEquals(ServiceRuntimeState.STOPPED, serviceA.getState());
    assertEquals(ServiceRuntimeState.STOPPED, serviceB.getState());

    launcher.awaitStop();
  }
}
