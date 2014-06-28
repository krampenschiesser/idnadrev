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

import de.ks.activity.context.ActivityStore;
import de.ks.application.PresentationArea;
import de.ks.executor.ExecutorService;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static de.ks.JunitMatchers.withRetry;
import static org.junit.Assert.*;

public class ActivityBindingTest extends AbstractActivityTest {
  private static final Logger log = LoggerFactory.getLogger(ActivityBindingTest.class);
  @Inject
  protected ActivityStore store;
  @Inject
  protected ExecutorService executorService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    activityController.start(activityCfg);
    activityController.waitForDataSource();
  }

  @Test
  public void testActivityStore() throws Exception {
    PresentationArea mainArea = navigator.getMainArea();
    Node currentNode = mainArea.getCurrentNode();
    assertNotNull(currentNode);
    TextField idInput = (TextField) currentNode.lookup("#id");
    assertNotNull(idInput);

    withRetry(() -> store.getModelProperty().get() != null);

    final ActivityModel model = store.getModel();
    assertNotNull(model);
    assertSame(model, CDI.current().select(ActivityStore.class).get().getModel());

    executorService.submit(() -> assertSame(model, CDI.current().select(ActivityStore.class).get().getModel())).get();
  }

  @Test
  public void testBindings() throws Exception {
    Node view = activityController.getCurrentNode();

    ActivityModel model = new ActivityModel();
    model.setId(42);
    model.setName("Hello Sauerland");
    store.setModel(model);
    FXPlatform.waitForFX();
    assertSame(model, store.getModelProperty().get());

    TextField idInput = (TextField) view.lookup("#id");
    TextField nameInput = (TextField) view.lookup("#name");

    assertEquals("42", idInput.getText());
    assertEquals(model.getName(), nameInput.getText());

    model.setId(13);
    store.getBinding().fireValueChangedEvent();
    FXPlatform.waitForFX();


    assertEquals("13", idInput.getText());

    idInput.setText("78");
    assertEquals(78, model.getId());

    idInput.setText("bla");
    assertEquals(78, model.getId());
  }

  @Test
  public void testRebinding() throws Exception {
    Node view = activityController.getCurrentNode();

    ActivityModel model = new ActivityModel();
    model.setId(42);
    model.setName("Hello Sauerland");
    store.setModel(model);
    FXPlatform.waitForFX();

    TextField idInput = (TextField) view.lookup("#id");
    TextField nameInput = (TextField) view.lookup("#name");

    assertEquals("42", idInput.getText());
    assertEquals(model.getName(), nameInput.getText());

    ActivityModel nextModel = new ActivityModel().setId(13).setName("Steak");
    store.setModel(nextModel);
    FXPlatform.waitForFX();

    assertEquals("13", idInput.getText());
    assertEquals(nextModel.getName(), nameInput.getText());

    model.setId(0);
    store.getBinding().fireValueChangedEvent();

    assertEquals("13", idInput.getText());
  }

  @Test
  public void testBindTable() throws Exception {
    ActivityModel model = new ActivityModel();
    model.setId(42);
    model.setName("Hello Sauerland");
    for (int i = 0; i < 10; i++) {
      DetailItem detailItem = new DetailItem();
      detailItem.setName("item" + i);
      detailItem.setDescription("descirption" + i);
      model.getDetailItems().add(detailItem);
    }

    store.setModel(model);
    FXPlatform.waitForFX();

    Pane detailView = activityController.getNodeForController(DetailController.class);
    @SuppressWarnings("unchecked") TableView<DetailItem> table = (TableView<DetailItem>) detailView.lookup("#detailItems");
    assertNotNull(table);

    assertEquals(10, table.getItems().size());
    assertEquals(2, table.getColumns().size());
    table.getSelectionModel().select(9, table.getColumns().get(1));
    ObservableList<TablePosition> selectedCells = table.getSelectionModel().getSelectedCells();
    assertEquals(1, selectedCells.size());
  }
}
