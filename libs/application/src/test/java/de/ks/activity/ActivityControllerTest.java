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

package de.ks.activity;

import de.ks.LauncherRunner;
import de.ks.activity.context.ActivityContext;
import de.ks.application.Navigator;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class ActivityControllerTest {

  private ExecutorService executorService;
  @Inject
  protected ActivityController controller;
  @Inject
  protected ActivityContext context;

  @Before
  public void setUp() throws Exception {
    Dummy.fail = false;
    ApplicationService service = Launcher.instance.getService(ApplicationService.class);
    Navigator.registerWithBorderPane(service.getStage());
    executorService = Executors.newCachedThreadPool();
    warmup();
  }

  protected void warmup() {
    ArrayList<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < 50; i++) {
      Future<?> future = executorService.submit(() -> {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          //
        }
      });
      futures.add(future);
    }
    waitForFutures(futures);
  }

  private void waitForFutures(ArrayList<Future<?>> futures) {
    futures.forEach(f -> {
      try {
        f.get();
      } catch (Exception e) {
        //
      }
    });
  }

  @After
  public void tearDown() throws Exception {
    Dummy.fail = false;
    controller.stopAll();
    executorService.shutdown();
    executorService.awaitTermination(5, TimeUnit.SECONDS);
  }

  @Test
  public void testMultipleStartsBlock() throws Exception {
    ArrayList<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      String name = "dummy" + i;
      Future<?> future = executorService.submit(() -> controller.startOrResume(new ActivityHint(DummyActivity.class, name, null)));
      futures.add(future);
    }
    waitForFutures(futures);
    controller.waitForTasks();
    //silently load one after another
  }

  @Test
  public void testFailedStart() throws Exception {
    Dummy.fail = true;
    ActivityHint activityHint = new ActivityHint(DummyActivity.class);
    try {
      controller.startOrResume(activityHint);
    } catch (Exception e) {
      //
    }
    assertNull(context.getCurrentActivity());
  }

  @Test
  public void testFailedStartResumePrevious() throws Exception {
    ActivityHint initialHint = new ActivityHint(DummyActivity.class);
    initialHint.setNextActivityId("init");
    controller.startOrResume(initialHint);


    Dummy.fail = true;
    ActivityHint activityHint = new ActivityHint(DummyActivity.class);
    try {
      controller.startOrResume(activityHint);
    } catch (Exception e) {
      //
    }


    assertNotNull(context.getCurrentActivity());
    assertEquals("init", context.getCurrentActivity());
    assertEquals("init", controller.getCurrentActivityId());
  }

  @Test
  public void testNoDuplicateStart() throws Exception {
    ActivityHint activityHint = new ActivityHint(DummyActivity.class);
    controller.startOrResume(activityHint);
    assertFalse(controller.getControllerInstance(Dummy.class).isResumed());
    controller.startOrResume(activityHint);
    assertFalse(controller.getControllerInstance(Dummy.class).isResumed());

  }
}