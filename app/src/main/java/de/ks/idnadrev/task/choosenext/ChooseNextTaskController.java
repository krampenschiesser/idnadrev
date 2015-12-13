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
package de.ks.idnadrev.task.choosenext;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class ChooseNextTaskController extends BaseController<List<Task>> {
  @FXML
  protected TextField availableTime;
  @FXML
  protected ListView<Task> taskList;
  @FXML
  protected Button startWork;
  @FXML
  protected Button chooseTask;
  @FXML
  protected ComboBox<String> contextSelection;

  @Inject
  NextTaskChooser chooser;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ObjectProperty<String> contextProperty = contextSelection.valueProperty();

    BooleanBinding empty = contextProperty.asString().isEmpty();
    BooleanBinding isNull = contextProperty.isNull();
    chooseTask.disableProperty().bind(empty.or(isNull).or(validationRegistry.invalidProperty()));


    ReadOnlyObjectProperty<Task> selectedItemProperty = taskList.getSelectionModel().selectedItemProperty();
    startWork.disableProperty().bind(selectedItemProperty.isNull().or(validationRegistry.invalidProperty()));

    taskList.setCellFactory(view -> new ListCell<Task>() {
      @Override
      protected void updateItem(Task item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
          setText(item.getName());
        } else {
          setText("");
        }
      }
    });
    taskList.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        if (!startWork.isDisabled()) {
          onStartWork();
        }
      }
    });

    validationRegistry.registerValidator(availableTime, new IntegerRangeValidator(3, 60 * 12));
    validationRegistry.registerValidator(availableTime, new NotEmptyValidator());

  }

  @Override
  public void onResume() {
    List<String> names = PersistentWork.projection(Context.class, false, c -> c.getName());
    contextSelection.setItems(FXCollections.observableArrayList(names));

    onChooseTask();
  }

  @Override
  public void onStart() {
    onResume();
  }

  @FXML
  public void onChooseTask() {
    if (!chooseTask.isDisabled()) {
      String context = contextSelection.getValue();
      Integer timeInMinutes = Integer.valueOf(availableTime.getText());
      Runnable runner = () -> {
        List<Task> tasks = chooser.getTasksSorted(timeInMinutes, context);
        controller.getJavaFXExecutor().submit(() -> {
          taskList.setItems(FXCollections.observableArrayList(tasks));
          if (!tasks.isEmpty()) {
            taskList.getSelectionModel().select(0);
          }
        });
      };
      store.executeCustomRunnable(runner);
    }
  }

  @FXML
  public void onStartWork() {
    Supplier currentSelection = () -> taskList.getSelectionModel().getSelectedItem();

    ActivityHint activityHint = new ActivityHint(WorkOnTaskActivity.class, controller.getCurrentActivityId());
    activityHint.setDataSourceHint(currentSelection);
    activityHint.setReturnToDatasourceHint(currentSelection);

    controller.startOrResume(activityHint);
  }
}
