/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.activity;

import de.ks.LauncherRunner;
import de.ks.activity.resource.ResourceActivity;
import de.ks.activity.resource.ResourceTestController;
import de.ks.application.Navigator;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class LocalResourceTest {
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
    controller.startOrResume(new ActivityHint(ResourceActivity.class));
    controller.waitForTasks();
    controller.waitForDataSource();
    ResourceTestController controllerInstance = controller.getControllerInstance(ResourceTestController.class);
    assertEquals("Steak", controllerInstance.getLabel().getText());
  }
}
