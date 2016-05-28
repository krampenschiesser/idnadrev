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
package de.ks.idnadrev.task.view;

import de.ks.idnadrev.adoc.AdocAccessor;
import de.ks.idnadrev.adoc.ui.AdocPreview;
import de.ks.idnadrev.crud.CRUDController;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.add.AddTaskActivity;
import de.ks.idnadrev.util.ButtonHelper;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.reactfx.EventStreams;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class ViewTasksController extends BaseController<List<Task>> {
  @FXML
  SplitPane split;
  @FXML
  CRUDController crudController;
  @FXML
  AdocPreview previewController;
  @FXML
  TaskTable taskTableController;
  @FXML
  TaskFilter taskFilterController;
  @FXML
  VBox root;

  @Inject
  ButtonHelper buttonHelper;
  @Inject
  AdocAccessor adocAccessor;

  private Button edit;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ReadOnlyObjectProperty<TreeItem<Task>> selectedTreeItem = taskTableController.getTaskTable().getSelectionModel().selectedItemProperty();
    BooleanBinding itemIsNull = Bindings.createBooleanBinding(() -> selectedTreeItem.getValue() == null, selectedTreeItem);

    edit = buttonHelper.createImageButton(localized.get("edit"), "edit.png", 24);
    edit.disableProperty().bind(itemIsNull);
    edit.setOnAction(e -> edit(selectedTreeItem.get().getValue()));

    crudController.getCenterButtonContainer().getChildren().add(edit);
    crudController.getDeleteButton().disableProperty().bind(itemIsNull);
    crudController.getDeleteButton().setOnAction(e -> delete(selectedTreeItem.get().getValue()));

    EventStreams.nonNullValuesOf(selectedTreeItem).map(TreeItem::getValue).filter(Objects::nonNull).subscribe(previewController::setSelectedTask);
    EventStreams.eventsOf(taskTableController.getTaskTable(), MouseEvent.MOUSE_CLICKED).filter(e -> e.getClickCount() > 1).filter(e -> selectedTreeItem.get() != null && selectedTreeItem.get().getValue() != null).subscribe(m -> edit(selectedTreeItem.get().getValue()));
  }

  private void delete(Task task) {
    adocAccessor.delete(task);
    controller.reload();
  }

  private void edit(Task task) {
    ActivityHint hint = new ActivityHint(AddTaskActivity.class, controller.getCurrentActivityId()).setDataSourceHint(() -> task);
    controller.startOrResume(hint);
  }
}
