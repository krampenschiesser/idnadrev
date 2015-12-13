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
package de.ks.idnadrev.overview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class OverviewScheduledController extends BaseController<OverviewModel> {

  private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

  @FXML
  protected TableView<Task> scheduledTasks;
  @FXML
  protected TableColumn<Task, String> name;
  @FXML
  protected TableColumn<Task, String> startTime;
  @FXML
  protected TableColumn<Task, String> endTime;
  @FXML
  protected ListView<Task> proposedTasks;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    proposedTasks.setCellFactory(v -> new ConvertingListCell<>(c -> c.getName()));

    name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
    startTime.setCellValueFactory(this::convertStartTime);
    endTime.setCellValueFactory(this::convertEndTime);

    name.prefWidthProperty().bind(proposedTasks.widthProperty().subtract(startTime.widthProperty()).subtract(endTime.widthProperty()));

    registerHandlers(scheduledTasks, scheduledTasks.getSelectionModel());
    registerHandlers(proposedTasks, proposedTasks.getSelectionModel());
  }

  private void registerHandlers(Node node, MultipleSelectionModel<Task> selectionModel) {
    Runnable run = () -> {
      Task selectedItem = selectionModel.getSelectedItem();
      if (selectedItem != null) {
        startWork(selectedItem);
      }
    };
    node.setOnMouseClicked(e -> {
      if (e.getClickCount() > 1) {
        run.run();
      }
    });
    node.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        run.run();
      }
    });
  }

  private void startWork(Task task) {
    ActivityHint activityHint = new ActivityHint(WorkOnTaskActivity.class, controller.getCurrentActivityId());
    activityHint.setDataSourceHint(() -> task);
    controller.startOrResume(activityHint);
  }

  protected ObservableValue<String> convertStartTime(TableColumn.CellDataFeatures<Task, String> param) {
    LocalTime scheduledTime = param.getValue().getSchedule().getScheduledTime();
    if (scheduledTime != null) {
      return new SimpleStringProperty(scheduledTime.format(formatter));
    } else {
      return new SimpleStringProperty();
    }
  }

  protected ObservableValue<String> convertEndTime(TableColumn.CellDataFeatures<Task, String> param) {
    Task task = param.getValue();
    LocalTime scheduledTime = task.getSchedule().getScheduledTime();
    if (scheduledTime != null) {
      scheduledTime = scheduledTime.plus(task.getEstimatedTime());
      return new SimpleStringProperty(scheduledTime.format(formatter));
    } else {
      return new SimpleStringProperty();
    }
  }

  @Override
  protected void onRefresh(OverviewModel model) {
    super.onRefresh(model);

    proposedTasks.getItems().clear();
    proposedTasks.getItems().addAll(model.getProposedTasks());

    scheduledTasks.getItems().clear();
    scheduledTasks.getItems().addAll(model.getScheduledTasks());
  }
}
