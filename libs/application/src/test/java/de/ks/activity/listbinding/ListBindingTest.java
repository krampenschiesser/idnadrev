/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.activity.listbinding;

import de.ks.JunitMatchers;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.DetailItem;
import de.ks.activity.context.ActivityContext;
import de.ks.application.Navigator;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(LauncherRunner.class)
public class ListBindingTest {
  protected Navigator navigator;
  @Inject
  protected ActivityController activityController;
  protected ActivityCfg activityCfg;

  @Before
  public void setUp() throws Exception {
    navigator = Navigator.registerWithBorderPane(Launcher.instance.getService(JavaFXService.class).getStage());
    activityCfg = new ListActivity();
    activityController.start(activityCfg);
    activityController.waitForDataSourceLoading();
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(activityCfg);
    ActivityContext.stopAll();
  }

  @Test
  public void testBindListDataSource() throws Exception {
    StackPane pane = activityController.getCurrentNode();
    @SuppressWarnings("unchecked") TableView<DetailItem> tableView = (TableView<DetailItem>) pane.getChildren().get(0);
    assertTrue(JunitMatchers.withRetry(() -> 2 == tableView.getItems().size()));
    assertEquals(2, tableView.getColumns().size());
  }
}
