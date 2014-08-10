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
package de.ks.activity.controllerbinding;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.application.Navigator;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import de.ks.option.Option;
import de.ks.persistence.PersistentWork;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class ControllerBindingTest {
  @Inject
  ActivityController controller;

  @Before
  public void setUp() throws Exception {
    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    Navigator.registerWithBorderPane(service.getStage());

    PersistentWork.deleteAllOf(Option.class);
    PersistentWork.persist(new Option("test").setValue(42));
    controller.startOrResume(new ActivityHint(BindingActivity.class));
  }

  @After
  public void tearDown() throws Exception {
    controller.stop(BindingActivity.class.getSimpleName());
  }

  @Test
  public void testNameBinding() throws Exception {
    controller.waitForTasks();
    GridPane gridPane = controller.getCurrentNode();
    TextField name = (TextField) gridPane.lookup("#name");
    assertEquals("test", name.getText());

    name.setText("Hello");
    TestBindingController ctrl = controller.getCurrentController();
    ctrl.save();
    controller.waitForTasks();

    Option hello = PersistentWork.forName(Option.class, "Hello");
    assertNotNull(hello);
  }
}
