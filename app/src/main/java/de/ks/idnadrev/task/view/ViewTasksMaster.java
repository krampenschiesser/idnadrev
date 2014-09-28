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
import de.ks.datasource.DataSource;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.persistence.PersistentWork;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ViewTasksMaster extends BaseController<List<Task>> {
  private static final Logger log = LoggerFactory.getLogger(ViewTasksMaster.class);
  @FXML
  protected TreeTableView<Task> tasksView;
  @FXML
  protected TreeTableColumn<Task, Task> taskViewNameColumn;
  @FXML
  protected TreeTableColumn<Task, String> taskViewEstimatedTimeColumn;
  @FXML
  protected TreeTableColumn<Task, String> taskViewCreationTimeColumn;
  @FXML
  protected Button moreBtn;

  @FXML
  protected TextField searchField;
  @FXML
  protected ComboBox<String> contextSelection;

  protected final ObservableList<Task> tasks = FXCollections.observableArrayList();
  private Map<Task, TreeItem<Task>> task2TreeItem = new HashMap<>();
  private final SimpleBooleanProperty disable = new SimpleBooleanProperty(false);
  private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Localized.get("fullDate"));

  private Predicate<Task> filter = t -> true;
  private PopOver popOver;
  private ChangeListener<Boolean> hideOnFocusLeave;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ChangeListener<String> listener = (observable, oldValue, newValue) -> {
      refreshFilter();
    };
    searchField.textProperty().addListener(listener);
    contextSelection.getSelectionModel().selectedItemProperty().addListener(listener);

    taskViewNameColumn.setCellFactory(param -> {
      TreeTableCell<Task, Task> cell = new TreeTableCell<Task, Task>() {
        @Override
        protected void updateItem(Task item, boolean empty) {
          super.updateItem(item, empty);
          String styleClassFinished = "taskViewFinished";
          String styleClassASAP = "taskViewAsap";

          getTreeTableRow().getStyleClass().remove(styleClassFinished);
          getTreeTableRow().getStyleClass().remove(styleClassASAP);

          if (item != null) {
            setText(item.getName());
            if (item.isFinished()) {
              getTreeTableRow().getStyleClass().add(styleClassFinished);
            }
            if (item.getState() == TaskState.ASAP) {
              getTreeTableRow().getStyleClass().add(styleClassASAP);
            }
          } else {
            setText("");
          }
        }
      };

      return cell;
    });
    taskViewNameColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue()));

    taskViewEstimatedTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(parseDuration(param.getValue().getValue().getEstimatedTime(), false)));
    taskViewCreationTimeColumn.setCellValueFactory(param -> {
      TreeItem<Task> treeItem = param.getValue();
      Task task = treeItem.getValue();
      LocalDateTime creationTime = task.getCreationTime();
      String formatted = dateFormat.format(creationTime);
      return new SimpleStringProperty(formatted);
    });

    CompletableFuture.supplyAsync(() -> PersistentWork.from(Context.class).stream().map(c -> c.getName()).collect(Collectors.toList()), controller.getExecutorService())//
            .thenAcceptAsync(contextNames -> {
              ObservableList<String> items = FXCollections.observableArrayList(contextNames);
              items.add(0, "");
              contextSelection.setItems(items);
            }, controller.getJavaFXExecutor());

    searchField.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        searchField.setText("");
        e.consume();
      }
    });

    this.hideOnFocusLeave = (fp, fo, fn) -> {
      if (!fn && popOver != null) {
        boolean needsToKeepFocus = activityInitialization.getControllerInstance(TaskFilterView.class).needsToKeepFocus();
        if (!needsToKeepFocus) {
          popOver.hide();
        }
      }
    };

    moreBtn.sceneProperty().addListener((p, o, n) -> {
      if (n == null && popOver != null) {
        popOver.hide();
        if (o != null) {
          o.getWindow().focusedProperty().removeListener(this.hideOnFocusLeave);
        }
      } else if (n != null) {
        ReadOnlyBooleanProperty focused = moreBtn.getScene().getWindow().focusedProperty();
        log.info("Hiding popover because focus left scene");
        focused.removeListener(this.hideOnFocusLeave);
        focused.addListener(this.hideOnFocusLeave);
      } else if (n == null && o != null) {
        o.getWindow().focusedProperty().removeListener(this.hideOnFocusLeave);
      }
    });
  }

  protected void refreshFilter() {
    filter = createFilter();
    TreeItem<Task> root = buildTreeStructure(new ArrayList<>(tasks));
    tasksView.setRoot(root);
    selectBest(root);
  }

  protected Predicate<Task> createFilter() {
    return task -> {
      boolean hasContextFilter = contextSelection.getValue() != null && !contextSelection.getValue().trim().isEmpty();
      if (hasContextFilter) {
        Context taskContext = task.getContext();
        Predicate<Context> filter = ctx -> ctx.getName().equals(contextSelection.getValue().trim());
        if (taskContext == null) {
          boolean foundMatchingContext = false;
          for (Task current = task; current.getParent() != null; current = current.getParent()) {
            Context parentContext = current.getParent().getContext();
            try {
              if (parentContext != null && filter.test(parentContext)) {
                foundMatchingContext = true;
              }
            } catch (Exception e) {
              log.error("Could not get context of {}", current.getParent().getName());
              throw e;
            }
          }
          if (!foundMatchingContext) {
            return false;
          }
        } else if (!taskContext.getName().equals(contextSelection.getValue().trim())) {
          return false;
        }
      }
      String nameSearch = searchField.textProperty().getValueSafe().trim().toLowerCase(Locale.ENGLISH);
      if (!nameSearch.isEmpty()) {
        if (task.getName().toLowerCase(Locale.ENGLISH).contains(nameSearch)) {
          return true;
        } else {
          return false;
        }
      }
      return true;
    };
  }

  private String parseDuration(Duration duration, boolean useShortFormat) {
    if (duration == null) {
      return null;
    } else {
      long hours = duration.toHours();
      if (hours == 0 && useShortFormat) {
        return duration.toMinutes() + Localized.get("duration.minutes");
      } else {
        long remainingMinutes = duration.minus(Duration.ofHours(hours)).toMinutes();
        return String.format("%02d", hours) + ":" + String.format("%02d", remainingMinutes) + Localized.get("duration.hours.short");
      }
    }
  }

  @FXML
  void onTableKeyReleased(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
//      startWork();
    }
  }

  @FXML
  public void showMoreFilters() {
    TaskFilterView filter = activityInitialization.getControllerInstance(TaskFilterView.class);
    Node filterView = activityInitialization.getViewForController(TaskFilterView.class);
    popOver = new PopOver(filterView);
    popOver.setDetachable(true);
    popOver.setDetached(true);
    popOver.setCornerRadius(4);
    popOver.show(moreBtn);
  }

  @Override
  protected void onRefresh(List<Task> loaded) {
    tasks.clear();
    tasks.addAll(loaded);
    TreeItem<Task> root = buildTreeStructure(loaded);
    tasksView.setRoot(root);
    selectBest(root);
  }

  private void selectBest(TreeItem<Task> root) {
    DataSource noncast = store.getDatasource();
    ViewTasksDS datasource = (ViewTasksDS) noncast;
    Task taskToSelect = datasource.getTaskToSelect();
    Platform.runLater(() -> {
      if (!root.getChildren().isEmpty()) {
        root.setExpanded(true);
        TreeItem<Task> treeItem = root.getChildren().get(0);

        if (taskToSelect != null && task2TreeItem.containsKey(taskToSelect)) {
          treeItem = task2TreeItem.get(taskToSelect);
        } else {
          Optional<Task> first = task2TreeItem.keySet().stream().filter(filter).findFirst();
          if (first.isPresent()) {
            treeItem = task2TreeItem.get(first.get());
          }
        }
        expandParents(treeItem);
        tasksView.getSelectionModel().select(treeItem);
      }
    });
  }

  private void expandParents(TreeItem<Task> treeItem) {
    for (; treeItem.getParent() != null; treeItem = treeItem.getParent()) {
      treeItem.setExpanded(true);
    }
  }

  protected TreeItem<Task> buildTreeStructure(List<Task> loaded) {
    TreeItem<Task> root = new TreeItem<>(new Task(Localized.get("all")) {
      {
        id = -1L;
      }
    });
    task2TreeItem = new HashMap<>(loaded.size());

    calculateTotalTime(loaded, root);
    loaded.forEach((task) -> {
      TreeItem<Task> treeItem = new TreeItem<>(task);
      task2TreeItem.put(task, treeItem);
    });
    loaded.stream().filter(filter).sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).forEach((task) -> {
      for (; task.getParent() != null; task = task.getParent()) {
        task2TreeItem.putIfAbsent(task.getParent(), new TreeItem<>(task.getParent()));
        TreeItem<Task> parentItem = task2TreeItem.get(task.getParent());
        TreeItem<Task> childItem = task2TreeItem.get(task);
        if (!parentItem.getChildren().contains(childItem)) {
          parentItem.getChildren().add(childItem);
        }
      }
      TreeItem<Task> treeItem = task2TreeItem.get(task);
      if (!root.getChildren().contains(treeItem)) {
        root.getChildren().add(treeItem);
      }
    });
    return root;
  }

  private void calculateTotalTime(List<Task> loaded, TreeItem<Task> root) {
    Duration total = Duration.ofHours(0);
    for (Task task : loaded) {
      total = total.plus(task.getEstimatedTime());
    }
    root.getValue().setEstimatedTime(total);
  }

  public TreeTableView<Task> getTasksView() {
    return tasksView;
  }

  public SimpleBooleanProperty getDisable() {
    return disable;
  }

  public TreeItem<Task> getTreeItem(Task parent) {
    return task2TreeItem.get(parent);
  }

  public ObservableList<Task> getTasks() {
    return tasks;
  }

  public String getSelectedContext() {
    return contextSelection.getSelectionModel().getSelectedItem();
  }

  public ComboBox<String> getContextSelection() {
    return contextSelection;
  }
}
