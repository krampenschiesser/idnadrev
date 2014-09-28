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

package de.ks.activity.initialization;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.application.Navigator;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(LauncherRunner.class)
public class ActivityInitializationTest {
  @Inject
  ActivityController controller;

  @Before
  public void setUp() throws Exception {
    ApplicationService service = Launcher.instance.getService(ApplicationService.class);
    Navigator.registerWithBorderPane(service.getStage());
  }

  @After
  public void tearDown() throws Exception {
    controller.stopAll();
  }

  @Test
  public void testLoadAdditionalController() throws Exception {
    controller.startOrResume(new ActivityHint(InitializationActivity.class));
    controller.waitForTasks();
    assertTrue(controller.getControllerInstance(InitalizationController.class).didLoadOtherController);
    assertNotNull(controller.getControllerInstance(InitalizationController.class).other);
    assertNotNull(controller.getControllerInstance(OtherController.class));

    PojoController pojoController = controller.getControllerInstance(PojoController.class);
    assertNotNull(pojoController);
    assertNotNull(pojoController.controller);
    assertNotNull(pojoController.getResources());
  }
}