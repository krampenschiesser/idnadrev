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

import de.ks.BaseController;
import de.ks.fxcontrols.cell.ConvertingListCell;
import de.ks.idnadrev.entity.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class OverviewScheduledController extends BaseController<OverviewModel> {

  private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

  @FXML
  private TableView<Task> scheduledTasks;
  @FXML
  private TableColumn<Task, String> name;
  @FXML
  private TableColumn<Task, String> startTime;
  @FXML
  private TableColumn<Task, String> endTime;
  @FXML
  private ListView<Task> proposedTasks;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    proposedTasks.setCellFactory(v -> new ConvertingListCell<>(c -> c.getName()));

    name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
    startTime.setCellValueFactory(this::convertStartTime);
    endTime.setCellValueFactory(this::convertEndTime);


    name.prefWidthProperty().bind(proposedTasks.widthProperty().subtract(startTime.widthProperty()).subtract(endTime.widthProperty()));
  }

  protected ObservableValue<String> convertStartTime(TableColumn.CellDataFeatures<Task, String> param) {
    LocalTime scheduledTime = param.getValue().getSchedule().getScheduledTime();
    return new SimpleStringProperty(scheduledTime.format(formatter));
  }

  protected ObservableValue<String> convertEndTime(TableColumn.CellDataFeatures<Task, String> param) {
    Task task = param.getValue();
    LocalTime scheduledTime = task.getSchedule().getScheduledTime();
    scheduledTime = scheduledTime.plus(task.getEstimatedTime());
    return new SimpleStringProperty(scheduledTime.format(formatter));
  }
}
