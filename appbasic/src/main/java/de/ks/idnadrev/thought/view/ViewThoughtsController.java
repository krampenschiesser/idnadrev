/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.thought.view;

import de.ks.idnadrev.adoc.AdocAccessor;
import de.ks.idnadrev.adoc.add.AddAdocActivity;
import de.ks.idnadrev.adoc.ui.AdocPreview;
import de.ks.idnadrev.crud.CRUDController;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.add.AddTaskActivity;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.idnadrev.util.ButtonHelper;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.reactfx.EventStreams;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewThoughtsController extends BaseController<List<Task>> {

  @FXML
  private VBox root;
  @FXML
  private SplitPane split;
  @FXML
  private StackPane crudContainer;
  @FXML
  private StackPane tableContainer;
  @FXML
  private StackPane previewContainer;

  private CRUDController crud;
  private ThoughtTable thoughtTableController;
  private AdocPreview thoughtPreviewController;

  private Button toTask;
  private Button toDocument;

  @Inject
  ButtonHelper buttonHelper;
  @Inject
  AdocAccessor adocAccessor;
  private Button edit;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CRUDController.load(activityInitialization, v -> crudContainer.getChildren().add(v), ctrl -> this.crud = ctrl);
    activityInitialization.loadAdditionalController(ThoughtTable.class, v -> tableContainer.getChildren().add(v), ctrl -> this.thoughtTableController = ctrl);
    activityInitialization.loadAdditionalController(AdocPreview.class, v -> previewContainer.getChildren().add(v), ctrl -> this.thoughtPreviewController = ctrl);

    ReadOnlyObjectProperty<Task> selectedItemProperty = thoughtTableController.getThoughtTable().getSelectionModel().selectedItemProperty();
    BooleanBinding itemIsNull = selectedItemProperty.isNull();
    crud.getDeleteButton().disableProperty().bind(itemIsNull);

    toTask = buttonHelper.createImageButton(localized.get("toTask"), "toTask.png", 24);
    toTask.disableProperty().bind(itemIsNull);
    toTask.setOnAction(e -> toTask(thoughtTableController.getThoughtTable().getSelectionModel().getSelectedItem()));
    edit = buttonHelper.createImageButton(localized.get("edit"), "edit.png", 24);
    edit.disableProperty().bind(itemIsNull);
    toDocument = buttonHelper.createImageButton(localized.get("toDocument"), "toDocument.png", 24);
    toDocument.setContentDisplay(ContentDisplay.RIGHT);
    toDocument.disableProperty().bind(itemIsNull);
    toDocument.setOnAction(e -> toDocument(thoughtTableController.getThoughtTable().getSelectionModel().getSelectedItem()));
    crud.getCenterButtonContainer().getChildren().addAll(toTask, edit, toDocument);

    thoughtPreviewController.selectedTaskProperty().bind(selectedItemProperty);

    edit.setOnAction(e -> edit(selectedItemProperty.get()));
    crud.getDeleteButton().setOnAction(e -> delete(selectedItemProperty.get()));

    EventStreams.eventsOf(thoughtTableController.getThoughtTable(), KeyEvent.KEY_RELEASED)//
      .filter(e -> e.getCode() == KeyCode.DELETE)//
      .subscribe(e -> delete(selectedItemProperty.get()));

    EventStreams.eventsOf(thoughtTableController.getThoughtTable(), MouseEvent.MOUSE_CLICKED).filter(e -> e.getClickCount() > 1).filter(e -> selectedItemProperty.get() != null).subscribe(m -> edit(selectedItemProperty.get()));
  }

  private void toDocument(Task task) {
    ActivityHint hint = new ActivityHint(AddAdocActivity.class, controller.getCurrentActivityId()).setDataSourceHint(() -> task);
    controller.startOrResume(hint);
  }

  private void toTask(Task task) {
    ActivityHint hint = new ActivityHint(AddTaskActivity.class, controller.getCurrentActivityId()).setDataSourceHint(() -> task);
    controller.startOrResume(hint);
  }

  private void delete(Task task) {
    adocAccessor.delete(task);
    controller.reload();
  }

  private void edit(Task task) {
    ActivityHint hint = new ActivityHint(AddThoughtActivity.class, controller.getCurrentActivityId()).setDataSourceHint(() -> task);
    controller.startOrResume(hint);
  }
}
