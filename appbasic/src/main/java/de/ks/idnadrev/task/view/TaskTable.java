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

import de.ks.idnadrev.adoc.Header;
import de.ks.idnadrev.task.Task;
import de.ks.standbein.BaseController;
import de.ks.standbein.table.TableColumnBuilder;
import de.ks.standbein.table.TableConfigurator;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TaskTable extends BaseController<List<Task>> {
  @FXML
  TreeTableView<Task> taskTable;
  protected TableColumn<Task, String> nameColumn;
  protected TableColumn<Task, String> repoColumn;

  @Inject
  TableConfigurator<Task> tableConfigurator;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tableConfigurator.addText(Task.class, Task::getTitle).setWidth(250);
    tableConfigurator.addNumber(Task.class, Task::getEstimatedTimeInMinutes).setWidth(100);
    tableConfigurator.addDateTime(Task.class, t -> t.getHeader().getRevDate()).setWidth(120);

    Function<Task, String> taskStringFunction = t -> t.getRepository().getName();
    TableColumnBuilder<Task> taskTableColumnBuilder = tableConfigurator.addText(Task.class, taskStringFunction);
    taskTableColumnBuilder.setWidth(100);
    tableConfigurator.configureTreeTable(taskTable);
    taskTable.setShowRoot(false);
  }

  public TreeTableView<Task> getTaskTable() {
    return taskTable;
  }

  @Override
  protected void onRefresh(List<Task> model) {
    TreeItem<Task> root = buildTreeStructure(model);
    taskTable.setRoot(root);
  }

  protected TreeItem<Task> buildTreeStructure(List<Task> loaded) {
    TreeItem<Task> root = new TreeItem<>(new Task(null, null, new Header(null)));
    HashMap<Task, TreeItem<Task>> task2TreeItem = new HashMap<>(loaded.size());

    loaded.forEach((task) -> {
      TreeItem<Task> treeItem = new TreeItem<>(task);
      task2TreeItem.put(task, treeItem);
    });
    Map<Path, Task> path2Task = loaded.stream().collect(Collectors.toMap(Task::getPath, task -> task));
    HashSet<Task> children = new HashSet<>();
    for (Task task : loaded) {
      TreeItem<Task> parent = task2TreeItem.get(task);
      Path parentDir = task.getPath().getParent();

      for (Map.Entry<Path, Task> entry : path2Task.entrySet()) {
        Task childTask = entry.getValue();
        Path path = entry.getKey();
        if (path.startsWith(parentDir) && !path.equals(task.getPath())) {
          TreeItem<Task> child = task2TreeItem.get(childTask);
          parent.getChildren().add(child);
          children.add(childTask);
        }
      }
    }
    HashSet<Task> rootItems = new HashSet<>(loaded);
    rootItems.removeAll(children);
    rootItems.stream().map(task2TreeItem::get).forEach(c -> root.getChildren().add(c));
    return root;
  }

}
