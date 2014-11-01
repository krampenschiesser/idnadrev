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
package de.ks.idnadrev.task.view;

import de.ks.BaseController;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class WorkUnitController extends BaseController<List<Task>> {

  @FXML
  private TableView<WorkUnit> WorkUnitTable;
  @FXML
  private TableColumn<WorkUnit, String> endColumn;
  @FXML
  private TableColumn<WorkUnit, String> startColumn;
  @FXML
  private TableColumn<WorkUnit, String> duration;
  @FXML
  private DatePicker date;
  @FXML
  private TextField start;
  @FXML
  private Button save;
  @FXML
  private TextField end;
  @FXML
  private GridPane root;
  private final SimpleObjectProperty<Task> task = new SimpleObjectProperty<>();
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Localized.get("fullDate"));

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    startColumn.setCellValueFactory(param -> createTimeStringProperty(param.getValue().getStart()));
    endColumn.setCellValueFactory(param -> createTimeStringProperty(param.getValue().getEnd()));

    task.addListener((p, o, n) -> {
      WorkUnitTable.getItems().clear();
      if (n != null) {
        PersistentWork.wrap(() -> {
          Task reload = PersistentWork.reload(n);
          WorkUnitTable.getItems().addAll(reload.getWorkUnits());
        });
      }
    });
  }

  private SimpleStringProperty createTimeStringProperty(LocalDateTime dateTime) {
    SimpleStringProperty retval = new SimpleStringProperty();
    if (dateTime != null) {
      retval.set(formatter.format(dateTime));
    }
    return retval;
  }

//  @Override
//  protected void onRefresh(Task model) {
//    super.onRefresh(model);
//
//    WorkUnitTable.getItems().clear();
//    WorkUnitTable.getItems().addAll(model.getWorkUnits());
//  }

//  @Override
//  public void duringLoad(Task model) {
//    model.getWorkUnits().forEach(u -> u.getStart());//lazy loading
//  }

  public GridPane getRoot() {
    return root;
  }

  public Task getTask() {
    return task.get();
  }

  public SimpleObjectProperty<Task> taskProperty() {
    return task;
  }

  public void setTask(Task task) {
    this.task.set(task);
  }
}
