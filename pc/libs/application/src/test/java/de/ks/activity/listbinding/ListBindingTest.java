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

import de.ks.JFXCDIRunner;
import de.ks.JunitMatchers;
import de.ks.activity.Activity;
import de.ks.activity.ActivityController;
import de.ks.activity.DetailItem;
import de.ks.activity.context.ActivityContext;
import de.ks.application.Navigator;
import de.ks.datasource.ListDataSource;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JFXCDIRunner.class)
public class ListBindingTest {
  protected Navigator navigator;
  @Inject
  protected ActivityController activityController;
  protected Activity activity;

  @Before
  public void setUp() throws Exception {
    navigator = Navigator.registerWithBorderPane(JFXCDIRunner.getStage());
    CountDownLatch latch = new CountDownLatch(1);

    ListDataSource<DetailItem> dataSource = new ListDataSource<DetailItem>() {

      @Override
      public List<DetailItem> loadModel() {
        return Arrays.asList(new DetailItem().setName("Name1").setDescription("Desc1"), new DetailItem().setName("Name2").setDescription("Desc2"));
      }

      @Override
      public void saveModel(List<DetailItem> items) {
        //
      }
    };

    activity = new ListActivity(dataSource, SimpleListView.class, activityController, navigator);
    activityController.start(activity);
    activityController.waitForDataSourceLoading();
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(activity);
    ActivityContext.stopAll();
  }


  @Test
  public void testBindListDataSource() throws Exception {
    StackPane pane = activity.getView(SimpleListView.class);
    @SuppressWarnings("unchecked") TableView<DetailItem> tableView = (TableView<DetailItem>) pane.getChildren().get(0);
    assertTrue(JunitMatchers.withRetry(() -> 2 == tableView.getItems().size()));
    assertEquals(2, tableView.getColumns().size());
  }
}
