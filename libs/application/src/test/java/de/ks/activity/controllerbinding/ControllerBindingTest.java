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
import de.ks.activity.context.ActivityStore;
import de.ks.application.Navigator;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import de.ks.util.FXPlatform;
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
  @Inject
  ActivityStore store;
  private TestBindingDS datasource;

  @Before
  public void setUp() throws Exception {
    ApplicationService service = Launcher.instance.getService(ApplicationService.class);
    Navigator.registerWithBorderPane(service.getStage());

    controller.startOrResume(new ActivityHint(BindingActivity.class));
    controller.waitForTasks();
    datasource = (TestBindingDS) store.getDatasource();
  }

  @After
  public void tearDown() throws Exception {
    controller.stopAll();
  }

  @Test
  public void testNameBinding() throws Exception {
    GridPane gridPane = controller.getCurrentNode();
    TextField name = (TextField) gridPane.lookup("#name");
    assertEquals("test", name.getText());

    FXPlatform.invokeLater(() -> name.setText("Hello"));
    TestBindingController ctrl = controller.getCurrentController();
    ctrl.save();
    controller.waitForTasks();


    Option hello = datasource.getSaved();
    assertNotNull(hello);
  }

  @Test
  public void testClearOnRefresh() throws Exception {
    GridPane gridPane = controller.getCurrentNode();
    TextField name = (TextField) gridPane.lookup("#name");
    assertEquals("test", name.getText());

    store.getBinding().registerClearOnRefresh(name);
    FXPlatform.invokeLater(() -> store.getBinding().getStringProperty(Option.class, o -> o.getName()).unbindBidirectional(name.textProperty()));

    controller.reload();
    controller.waitForDataSource();

    assertEquals("", name.getText());
  }
}
