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
import de.ks.activity.initialization.LoadInFXThread;
import de.ks.activity.link.NavigationHint;
import de.ks.datasource.DataSource;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.file.FileStore;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.finish.FinishTaskActivity;
import de.ks.idnadrev.task.work.WorkOnTaskActivity;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import de.ks.reflection.PropertyPath;
import de.ks.text.view.AsciiDocContent;
import de.ks.text.view.AsciiDocViewer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@LoadInFXThread
public class ViewTasks extends BaseController<List<Task>> {
  private static final Logger log = LoggerFactory.getLogger(ViewTasks.class);
  public static final String NEGATIVE_FUN_FACTOR = "negativeFunFactor";
  public static final String RECOVERING_EFFORT = "recoveringEffortFactor";
  @FXML
  protected TreeTableView<Task> tasksView;
  @FXML
  protected TreeTableColumn<Task, String> taskViewNameColumn;
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
  protected TextField searchField;
  @FXML
  protected ComboBox<String> contextSelection;

  @Inject
  FileStore fileStore;

  protected ObservableList<Task> tasks = FXCollections.observableArrayList();
  private Map<Task, TreeItem<Task>> task2TreeItem = new HashMap<>();
  private final SimpleBooleanProperty disable = new SimpleBooleanProperty(false);
  private AsciiDocViewer asciiDocViewer;
  private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Localized.get("fullDate"));

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
      ViewTasksDS datasource = (ViewTasksDS) store.getDatasource();
      datasource.setFilter(createFilter());
      lastExecutionGroup.schedule(() -> "").thenRunAsync(() -> controller.reload(), controller.getCurrentExecutorService());
    };
    searchField.textProperty().addListener(listener);
    contextSelection.getSelectionModel().selectedItemProperty().addListener(listener);

    ReadOnlyObjectProperty<TreeItem<Task>> selectedItemProperty = tasksView.getSelectionModel().selectedItemProperty();
    selectedItemProperty.addListener((p, o, n) -> applyTask(n));
    taskViewNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getName()));
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

    CompletableFuture.supplyAsync(() -> PersistentWork.from(Context.class).stream().map(c -> c.getName()).collect(Collectors.toList()), controller.getCurrentExecutorService())//
            .thenAcceptAsync(contextNames -> {
              ObservableList<String> items = FXCollections.observableArrayList(contextNames);
              items.add(0, "");
              contextSelection.setItems(items);
            }, controller.getJavaFXExecutor());
  }

  protected QueryConsumer<Task> createFilter() {
    return (root, query, builder) -> {
      ArrayList<Predicate> restrictions = new ArrayList<>();
      if (query.getRestriction() != null) {
        restrictions.add(query.getRestriction());
      }
      if (searchField.getText() != null) {
        restrictions.add(builder.like(builder.lower(root.get("name")), "%" + searchField.getText().toLowerCase() + "%"));
      }
      if (contextSelection.getValue() != null && !contextSelection.getValue().trim().isEmpty()) {
        Path<String> contextNamePath = root.get(PropertyPath.property(Task.class, t -> t.getContext())).<String>get("name");
        restrictions.add(builder.equal(contextNamePath, contextSelection.getValue()));
      }
      query.where(restrictions.toArray(new Predicate[restrictions.size()]));
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

  @Override
  protected void onRefresh(List<Task> loaded) {
    DataSource noncast = store.getDatasource();
    @SuppressWarnings("unchecked") //
            ViewTasksDS datasource = (ViewTasksDS) noncast;
    Task taskToSelect = datasource.getTaskToSelect();

    tasks.clear();
    tasks.addAll(loaded);
    TreeItem<Task> root = buildTreeStructure(loaded);
    tasksView.setRoot(root);
    Platform.runLater(() -> {
      if (!root.getChildren().isEmpty()) {
        root.setExpanded(true);
        if (taskToSelect != null && task2TreeItem.containsKey(taskToSelect)) {
          tasksView.getSelectionModel().select(task2TreeItem.get(taskToSelect));
        } else {
          tasksView.getSelectionModel().select(root.getChildren().get(0));
        }
      }
    });

    List<AsciiDocContent> asciiDocContents = tasks.stream().map(t -> new AsciiDocContent(t.getName(), t.getDescription())).collect(Collectors.toList());
    this.asciiDocViewer.preload(asciiDocContents);
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
    loaded.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).forEach((task) -> {
      if (task.getParent() == null) {
        root.getChildren().add(task2TreeItem.get(task));
      } else {
        TreeItem<Task> parentItem = task2TreeItem.get(task.getParent());
        TreeItem<Task> childItem = task2TreeItem.get(task);
        parentItem.getChildren().add(childItem);
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
