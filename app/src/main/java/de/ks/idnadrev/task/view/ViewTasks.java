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

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.finish.FinishTaskActivity;
import de.ks.idnadrev.task.work.WorkOnTaskActivity;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.texteditor.preview.TextPreview;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.FileStore;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class ViewTasks extends BaseController<List<Task>> {
  private static final Logger log = LoggerFactory.getLogger(ViewTasks.class);
  public static final String NEGATIVE_FUN_FACTOR = "negativeFunFactor";
  public static final String RECOVERING_EFFORT = "recoveringEffortFactor";

  @FXML
  protected ViewTasksMaster viewController;

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
  protected Button createSubtaskBtn;
  @FXML
  protected Button show;
  @FXML
  protected Button delete;
  @FXML
  protected Button later;
  @FXML
  protected Button asap;

  @Inject
  FileStore fileStore;
  @Inject
  PersistentWork persistentWork;

  private TextPreview asciiDocViewer;
  private final SimpleBooleanProperty disable = new SimpleBooleanProperty(false);

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TreeTableView<Task> tasksView = viewController.getTasksView();

    TextPreview.load(activityInitialization, view -> description.getChildren().add(view), ctrl -> asciiDocViewer = ctrl);

    tasksView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n == null) {
        asciiDocViewer.clear();
      } else {
        asciiDocViewer.show(n.getValue().getAdocFile().getTmpFile());
      }
    });

    viewController.getTasksView().setOnMouseClicked(e -> {
      if (e.getClickCount() > 1 && !start.isDisabled()) {
        startWork();
      }
    });

    ReadOnlyObjectProperty<TreeItem<Task>> selectedItemProperty = tasksView.getSelectionModel().selectedItemProperty();
    selectedItemProperty.addListener((p, o, n) -> applyTask(n));

    start.disableProperty().bind(disable);
    finish.disableProperty().bind(disable);
    edit.disableProperty().bind(disable);
    show.disableProperty().bind(disable);
    delete.disableProperty().bind(disable);
    later.disableProperty().bind(disable);
    asap.disableProperty().bind(disable);
    createSubtaskBtn.disableProperty().bind(disable);
  }

  protected void applyTask(TreeItem<Task> taskTreeItem) {
    disable.set(true);
    clear();
    if (taskTreeItem != null) {
      Task task = taskTreeItem.getValue();
      log.info("Applying task {}", task);
      if (task.getId() != null) {
        disable.set(false);
      }
      name.setText(task.getName());
      context.setText(task.getContext() != null ? task.getContext().getName() : "");
      estimatedTime.setText(parseDuration(task.isProject() ? task.getTotalEstimatedTime() : task.getEstimatedTime(), true));
      spentTime.setText(parseDuration(task.getTotalWorkDuration(), true));
      parentProject.setText(task.getParent() != null ? task.getParent().getName() : null);

      state.setText(task.getState().name());
      setEffortProgress(task.getPhysicalEffort().getAmount(), physicalEffort, RECOVERING_EFFORT);
      setEffortProgress(task.getMentalEffort().getAmount(), mentalEffort, RECOVERING_EFFORT);
      setEffortProgress(task.getFunFactor().getAmount(), funFactor, NEGATIVE_FUN_FACTOR);

      task.getTags().forEach((tag) -> tagPane.getChildren().add(new Label(tag.getDisplayName())));

      asciiDocViewer.show(task.getAdocFile().getTmpFile());
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

  private String parseDuration(Duration duration, boolean useShortFormat) {
    if (duration == null) {
      return null;
    } else {
      long hours = duration.toHours();
      if (hours == 0 && useShortFormat) {
        return duration.toMinutes() + localized.get("duration.minutes");
      } else {
        long remainingMinutes = duration.minus(Duration.ofHours(hours)).toMinutes();
        return String.format("%02d", hours) + ":" + String.format("%02d", remainingMinutes) + localized.get("duration.hours.short");
      }
    }
  }

  private void clear() {
    name.setText(null);
    spentTime.setText(null);
    asciiDocViewer.clear();
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
    TreeTableView<Task> tasksView = viewController.getTasksView();
    Task parent = tasksView.getSelectionModel().getSelectedItem().getValue().getParent();
    TreeItem<Task> nextSelection = viewController.getTreeItem(parent);
    tasksView.getSelectionModel().select(nextSelection);
  }

  @FXML
  void showTimeUnits() {
    Stage stage = new Stage();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initOwner(this.show.getScene().getWindow());
    stage.setTitle(localized.get("workunits"));

    WorkUnitController workUnitController = controller.getControllerInstance(WorkUnitController.class);
    Task value = viewController.getTasksView().getSelectionModel().getSelectedItem().getValue();
    workUnitController.setTask(value);

    GridPane root = workUnitController.getRoot();

    stage.setScene(new Scene(root));
    stage.show();
    stage.setOnHidden(e -> {
      stage.getScene().setRoot(new StackPane());
    });
  }

  @FXML
  void editTask() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    ActivityHint hint = new ActivityHint(CreateTaskActivity.class);
    hint.setReturnToActivity(controller.getCurrentActivityId());

    Supplier supplier = () -> tasksView.getSelectionModel().getSelectedItem().getValue();
    hint.setReturnToDatasourceHint(supplier);
    hint.setDataSourceHint(supplier);

    controller.startOrResume(hint);
  }

  @FXML
  void createSubtask() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    ActivityHint hint = new ActivityHint(CreateTaskActivity.class);
    hint.setReturnToActivity(controller.getCurrentActivityId());

    Supplier supplier = () -> {
      Task parent = tasksView.getSelectionModel().getSelectedItem().getValue();
      Task child = new Task("");
      child.setParent(parent);
      child.setContext(parent.getContext());
      return child;
    };
    hint.setReturnToDatasourceHint(() -> tasksView.getSelectionModel().getSelectedItem().getValue());
    hint.setDataSourceHint(supplier);

    controller.startOrResume(hint);
  }

  @FXML
  void startWork() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    Supplier currentSelection = () -> tasksView.getSelectionModel().getSelectedItem().getValue();

    ActivityHint activityHint = new ActivityHint(WorkOnTaskActivity.class, controller.getCurrentActivityId());
    activityHint.setDataSourceHint(currentSelection);
    activityHint.setReturnToDatasourceHint(currentSelection);

    controller.startOrResume(activityHint);
  }

  @FXML
  void finishTask() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    ActivityHint activityHint = new ActivityHint(FinishTaskActivity.class, controller.getCurrentActivityId());
    activityHint.setDataSourceHint(() -> tasksView.getSelectionModel().getSelectedItem().getValue());

    controller.startOrResume(activityHint);
  }

  @FXML
  void deleteTask() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    persistentWork.run(em -> {
      Task task = tasksView.getSelectionModel().getSelectedItem().getValue();
      em.remove(persistentWork.reload(task));
    });
    controller.reload();
  }

  @FXML
  public void scheduleAsap() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    persistentWork.run(em -> {
      Task task = tasksView.getSelectionModel().getSelectedItem().getValue();
      persistentWork.reload(task).setState(TaskState.ASAP);
    });
    controller.reload();
  }

  @FXML
  public void scheduleLater() {
    TreeTableView<Task> tasksView = viewController.getTasksView();
    persistentWork.run(em -> {
      Task task = tasksView.getSelectionModel().getSelectedItem().getValue();
      persistentWork.reload(task).setState(TaskState.LATER);
    });
    controller.reload();
  }

  @Override
  protected void onRefresh(List<Task> loaded) {
    loaded.forEach(t -> {
      AdocFile adocFile = t.getAdocFile();
      asciiDocViewer.preload(adocFile.getTmpFile(), adocFile.getRenderingPath(), adocFile.getContent());
    });
  }
}
