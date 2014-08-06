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
import de.ks.activity.link.NavigationHint;
import de.ks.datasource.DataSource;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.file.FileStore;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.finish.FinishTaskActivity;
import de.ks.idnadrev.task.work.WorkOnTaskActivity;
import de.ks.persistence.PersistentWork;
import de.ks.text.view.AsciiDocContent;
import de.ks.text.view.AsciiDocViewer;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ViewTasks extends BaseController<List<Task>> {
  private static final Logger log = LoggerFactory.getLogger(ViewTasks.class);
  public static final String NEGATIVE_FUN_FACTOR = "negativeFunFactor";
  public static final String RECOVERING_EFFORT = "recoveringEffortFactor";
  @FXML
  protected TreeTableView<Task> tasksView;
  @FXML
  protected TreeTableColumn<Task, Task> taskViewNameColumn;
  @FXML
  protected TreeTableColumn<Task, String> taskViewEstimatedTimeColumn;
  @FXML
  protected TreeTableColumn<Task, String> taskViewCreationTimeColumn;
  @FXML
  protected Label name;
  @FXML
  protected Label context;
  @FXML
  protected Label estimatedTime;
  @FXML
  protected Label spentTime;
  @FXML
  protected Label state;
  @FXML
  protected Hyperlink parentProject;
  @FXML
  protected ProgressBar physicalEffort;
  @FXML
  protected ProgressBar mentalEffort;
  @FXML
  protected ProgressBar funFactor;
  @FXML
  protected FlowPane tagPane;
  @FXML
  protected StackPane description;
  @FXML
  protected Button start;
  @FXML
  protected Button finish;
  @FXML
  protected Button edit;
  @FXML
  protected Button show;
  @FXML
  protected Button delete;
  @FXML
  protected Button later;
  @FXML
  protected Button asap;
  @FXML
  protected Button moreBtn;

  @FXML
  protected TextField searchField;
  @FXML
  protected ComboBox<String> contextSelection;

  @Inject
  FileStore fileStore;

  protected final ObservableList<Task> tasks = FXCollections.observableArrayList();
  private Map<Task, TreeItem<Task>> task2TreeItem = new HashMap<>();
  private final SimpleBooleanProperty disable = new SimpleBooleanProperty(false);
  private AsciiDocViewer asciiDocViewer;
  private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Localized.get("fullDate"));

  private Predicate<Task> filter = t -> true;
  private Dialog dialog;
  private PopOver popOver;
  private ChangeListener<Boolean> hideOnFocusLeave;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    activityInitialization.loadAdditionalController(AsciiDocViewer.class).thenAcceptAsync(l -> {
      asciiDocViewer = l.getController();
      asciiDocViewer.addPreProcessor(fileStore::replaceFileStoreDir);
      tasksView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
        if (n == null) {
          asciiDocViewer.reset();
        } else {
          asciiDocViewer.show(new AsciiDocContent(n.getValue().getName(), n.getValue().getDescription()));
        }
      });
      description.getChildren().add(l.getView());
    }, controller.getJavaFXExecutor());

    LastExecutionGroup<String> lastExecutionGroup = new LastExecutionGroup<>(300, controller.getCurrentExecutorService());
    ChangeListener<String> listener = (observable, oldValue, newValue) -> {
      refreshFilter();
    };
    searchField.textProperty().addListener(listener);
    contextSelection.getSelectionModel().selectedItemProperty().addListener(listener);

    ReadOnlyObjectProperty<TreeItem<Task>> selectedItemProperty = tasksView.getSelectionModel().selectedItemProperty();
    selectedItemProperty.addListener((p, o, n) -> applyTask(n));
    taskViewNameColumn.setCellFactory(param -> {
      TreeTableCell<Task, Task> cell = new TreeTableCell<Task, Task>() {
        @Override
        protected void updateItem(Task item, boolean empty) {
          super.updateItem(item, empty);
          if (item != null) {
            setText(item.getName());
            if (item.isFinished()) {
              getTreeTableRow().getStyleClass().add("taskViewFinished");
            } else {
              getTreeTableRow().getStyleClass().remove("taskViewFinished");
            }
          } else {
            setText("");
            getTreeTableRow().getStyleClass().remove("taskViewFinished");
          }
        }
      };

      return cell;
    });
    taskViewNameColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue()));

    taskViewEstimatedTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(parseDuration(param.getValue().getValue().getEstimatedTime())));
    taskViewCreationTimeColumn.setCellValueFactory(param -> {
      TreeItem<Task> treeItem = param.getValue();
      Task task = treeItem.getValue();
      LocalDateTime creationTime = task.getCreationTime();
      String formatted = dateFormat.format(creationTime);
      return new SimpleStringProperty(formatted);
    });

    start.disableProperty().bind(disable);
    finish.disableProperty().bind(disable);
    edit.disableProperty().bind(disable);
    show.disableProperty().bind(disable);
    delete.disableProperty().bind(disable);
    later.disableProperty().bind(disable);
    asap.disableProperty().bind(disable);

    CompletableFuture.supplyAsync(() -> PersistentWork.from(Context.class).stream().map(c -> c.getName()).collect(Collectors.toList()), controller.getCurrentExecutorService())//
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
        popOver.hide();
      }
    };
    moreBtn.sceneProperty().addListener((p, o, n) -> {
      if (n == null && popOver != null) {
        popOver.hide();
      } else if (n != null) {
        ReadOnlyBooleanProperty focused = moreBtn.getScene().getWindow().focusedProperty();
        focused.removeListener(this.hideOnFocusLeave);
        focused.addListener(this.hideOnFocusLeave);
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
    TaskFilterView filterView = activityInitialization.getControllerInstance(TaskFilterView.class);

    return task -> {
      boolean hasContextFilter = contextSelection.getValue() != null && !contextSelection.getValue().trim().isEmpty();
      if (hasContextFilter) {
        Context taskContext = task.getContext();
        Predicate<Context> filter = ctx -> ctx.getName().equals(contextSelection.getValue().trim());
        if (taskContext == null) {
          boolean foundMatchingContext = false;
          for (Task current = task; current.getParent() != null; current = current.getParent()) {
            Context parentContext = current.getParent().getContext();
            if (parentContext != null && filter.test(parentContext)) {
              foundMatchingContext = true;
            }
          }
          if (!foundMatchingContext) {
            return false;
          }
        } else if (!taskContext.getName().equals(contextSelection.getValue().trim())) {
          return false;
        }
      }
      String nameSearch = searchField.textProperty().getValueSafe().trim().toLowerCase();
      if (!nameSearch.isEmpty()) {
        if (task.getName().toLowerCase().contains(nameSearch)) {
          return true;
        } else {
          return false;
        }
      }
      return true;
    };
  }

  protected void applyTask(TreeItem<Task> taskTreeItem) {
    disable.set(true);
    clear();
    if (taskTreeItem != null) {
      Task task = taskTreeItem.getValue();
      log.info("Applying task {}", task);
      if (task.getId() >= 0) {
        disable.set(false);
      }
      name.setText(task.getName());
      context.setText(task.getContext() != null ? task.getContext().getName() : "");
      estimatedTime.setText(parseDuration(task.isProject() ? task.getTotalEstimatedTime() : task.getEstimatedTime()));
      spentTime.setText(parseDuration(task.getTotalWorkDuration()));
      parentProject.setText(task.getParent() != null ? task.getParent().getName() : null);

      state.setText(task.getState().name());
      setEffortProgress(task.getPhysicalEffort().getAmount(), physicalEffort, RECOVERING_EFFORT);
      setEffortProgress(task.getMentalEffort().getAmount(), mentalEffort, RECOVERING_EFFORT);
      setEffortProgress(task.getFunFactor().getAmount(), funFactor, NEGATIVE_FUN_FACTOR);

      task.getTags().forEach((tag) -> tagPane.getChildren().add(new Label(tag.getName())));

      asciiDocViewer.show(new AsciiDocContent(task.getName(), task.getDescription()));
    }
  }

  private void setEffortProgress(int amount, ProgressBar temp, String styleClass) {
    temp.setProgress(Math.abs(amount / 5D));
    if (amount < 0) {
      temp.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
      temp.getStyleClass().add(styleClass);
    } else {
      temp.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
      temp.getStyleClass().remove(styleClass);
    }
  }

  private String parseDuration(Duration duration) {
    if (duration == null) {
      return null;
    } else {
      long hours = duration.toHours();
      if (hours == 0) {
        return duration.toMinutes() + Localized.get("duration.minutes");
      } else {
        long remainingMinutes = duration.minus(Duration.ofHours(hours)).toMinutes();
        return hours + ":" + remainingMinutes + Localized.get("duration.hours.short");
      }
    }
  }

  private void clear() {
    name.setText(null);
    spentTime.setText(null);
    asciiDocViewer.reset();
    parentProject.setText(null);
    context.setText(null);
    physicalEffort.setProgress(0);
    mentalEffort.setProgress(0);
    funFactor.setProgress(0);
    tagPane.getChildren().clear();
  }

  @FXML
  void onTableKeyReleased(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      startWork();
    }
  }

  @FXML
  void selectParentProject() {
    Task parent = tasksView.getSelectionModel().getSelectedItem().getValue().getParent();
    TreeItem<Task> nextSelection = task2TreeItem.get(parent);
    tasksView.getSelectionModel().select(nextSelection);
  }

  @FXML
  void showTimeUnits() {

  }

  @FXML
  void editTask() {
    NavigationHint hint = new NavigationHint();
    hint.setReturnToActivity(controller.getCurrentActivity());
    hint.setReturnToDatasourceHint(() -> tasksView.getSelectionModel().getSelectedItem().getValue());
    hint.setDataSourceHint(() -> tasksView.getSelectionModel().getSelectedItem().getValue());
    controller.start(CreateTaskActivity.class, hint);
  }

  @FXML
  void startWork() {
    Supplier currentSelection = () -> tasksView.getSelectionModel().getSelectedItem().getValue();

    NavigationHint navigationHint = new NavigationHint(controller.getCurrentActivity());
    navigationHint.setDataSourceHint(currentSelection);
    navigationHint.setReturnToDatasourceHint(currentSelection);

    controller.start(WorkOnTaskActivity.class, navigationHint);
  }

  @FXML
  void finishTask() {
    NavigationHint navigationHint = new NavigationHint(controller.getCurrentActivity());
    navigationHint.setDataSourceHint(() -> tasksView.getSelectionModel().getSelectedItem().getValue());

    controller.start(FinishTaskActivity.class, navigationHint);
  }

  @FXML
  void deleteTask() {
    PersistentWork.run(em -> {
      Task task = tasksView.getSelectionModel().getSelectedItem().getValue();
      em.remove(PersistentWork.reload(task));
    });
    controller.reload();
  }

  @FXML
  public void scheduleAsap() {
    PersistentWork.run(em -> {
      Task task = tasksView.getSelectionModel().getSelectedItem().getValue();
      PersistentWork.reload(task).setState(TaskState.ASAP);
    });
  }

  @FXML
  public void scheduleLater() {
    PersistentWork.run(em -> {
      Task task = tasksView.getSelectionModel().getSelectedItem().getValue();
      PersistentWork.reload(task).setState(TaskState.LATER);
    });
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

    List<AsciiDocContent> asciiDocContents = tasks.stream().map(t -> new AsciiDocContent(t.getName(), t.getDescription())).collect(Collectors.toList());
    this.asciiDocViewer.preload(asciiDocContents);
  }

  private void selectBest(TreeItem<Task> root) {
    DataSource noncast = store.getDatasource();
    ViewTasksDS datasource = (ViewTasksDS) noncast;
    Task taskToSelect = datasource.getTaskToSelect();
    Platform.runLater(() -> {
      if (!root.getChildren().isEmpty()) {
        root.setExpanded(true);
        if (taskToSelect != null && task2TreeItem.containsKey(taskToSelect)) {
          tasksView.getSelectionModel().select(task2TreeItem.get(taskToSelect));
        } else {
          Optional<Task> first = task2TreeItem.keySet().stream().filter(filter).findFirst();
          if (first.isPresent()) {
            TreeItem<Task> treeItem = task2TreeItem.get(first.get());
            expandParents(treeItem);
            tasksView.getSelectionModel().select(treeItem);
          } else {
            tasksView.getSelectionModel().select(root.getChildren().get(0));
          }
        }
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

}
