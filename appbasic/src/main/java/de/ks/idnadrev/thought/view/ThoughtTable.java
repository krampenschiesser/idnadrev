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

import de.ks.idnadrev.task.Task;
import de.ks.standbein.BaseController;
import de.ks.standbein.table.TableColumnBuilder;
import de.ks.standbein.table.TableConfigurator;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

public class ThoughtTable extends BaseController<List<Task>> {

  @FXML
  protected TableView<Task> thoughtTable;
  protected TableColumn<Task, String> nameColumn;
  protected TableColumn<Task, String> repoColumn;

  @Inject
  TableConfigurator<Task> tableConfigurator;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tableConfigurator.addText(Task.class, Task::getTitle).setWidth(250);
    Function<Task, String> taskStringFunction = t -> t.getRepository().getName();
    TableColumnBuilder<Task> taskTableColumnBuilder = tableConfigurator.addText(Task.class, taskStringFunction);
    taskTableColumnBuilder.setWidth(200);
    tableConfigurator.configureTable(thoughtTable);
  }

  public TableView<Task> getThoughtTable() {
    return thoughtTable;
  }

  @Override
  protected void onRefresh(List<Task> model) {
    thoughtTable.getItems().clear();
    thoughtTable.getItems().addAll(model);
  }
}
