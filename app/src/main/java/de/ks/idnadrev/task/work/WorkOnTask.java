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
package de.ks.idnadrev.task.work;

import de.ks.flatadocdb.entity.BaseEntity;
import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.IdnadrevWindow;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.idnadrev.task.finish.FinishTaskActivity;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.texteditor.TextEditor;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class WorkOnTask extends BaseController<Task> {
  private static final Logger log = LoggerFactory.getLogger(WorkOnTask.class);
  public static final String OVERTIME_STYLE_CLASS = "negativeFunFactor";
  @FXML
  protected Label estimatedTime;
  @FXML
  protected ProgressBar estimatedTimeBar;
  @FXML
  protected Label name;
  @FXML
  protected Label overTime;
  @FXML
  protected StackPane descriptionView;
  protected TextEditor description;

  @Inject
  IdnadrevWindow window;
  @Inject
  PersistentWork persistentWork;

  protected final SimpleStringProperty tookString = new SimpleStringProperty("");

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextEditor.load(activityInitialization, descriptionView.getChildren()::add, ade -> this.description = ade);

    StringProperty nameBinding = store.getBinding().getStringProperty(Task.class, t -> t.getName());
    name.textProperty().bind(nameBinding);

    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bind(description.textProperty());

    overTime.textProperty().isNotEmpty().addListener((p, o, n) -> {
      if (n) {
        log.info("Switching to negative style class");
        estimatedTimeBar.getStyleClass().add(OVERTIME_STYLE_CLASS);
      } else {
        log.info("Switching to positive style class");
        estimatedTimeBar.getStyleClass().remove(OVERTIME_STYLE_CLASS);
      }
    });
    store.getBinding().registerClearOnRefresh(overTime);

    controller.getJavaFXExecutor().submit(() -> {
      Tooltip tooltip = new Tooltip();
      tooltip.textProperty().bind(tookString);
      estimatedTimeBar.setTooltip(tooltip);
    });
  }

  @Override
  public void onSuspend() {
    controller.save();
  }

  @FXML
  void stopWork() {
    store.executeCustomRunnable(this::finishWorkUnit);
    controller.save();
    controller.stopCurrent();
  }

  protected void finishWorkUnit() {
    persistentWork.run(session -> {
      Task reload = persistentWork.reload(store.getModel());
      WorkUnit last = reload.getWorkUnits().last();
      last.stop();
    });
    controller.getJavaFXExecutor().submit(() -> window.getWorkingOnTaskLink().setCurrentTask(null));
  }

  @FXML
  void createThought() {
    ActivityHint hint = new ActivityHint(AddThoughtActivity.class, controller.getCurrentActivityId());
    controller.startOrResume(hint);
  }

  @FXML
  void finishTask() {
    store.executeCustomRunnable(this::finishWorkUnit);
    controller.save();
    ActivityHint currentHint = controller.getCurrentActivity().getActivityHint();

    String returnToActivity = controller.getCurrentActivityId();
    if (currentHint != null) {
      returnToActivity = currentHint.getReturnToActivity();
    }
    ActivityHint nextHint = new ActivityHint(FinishTaskActivity.class, returnToActivity);

    BaseEntity model = store.getModel();
    nextHint.setDataSourceHint(() -> persistentWork.reload(model));
    controller.startOrResume(nextHint);
  }

  @Override
  protected void onRefresh(Task task) {
    String descriptionContent = task.getDescription();
    description.setText(descriptionContent);

    ActivityExecutor executorService = controller.getExecutorService();
    executorService.submit(() -> {
      persistentWork.run(session -> {
        Task reloaded = persistentWork.reload(task);
        WorkUnit last = reloaded.getWorkUnits().isEmpty() ? null : reloaded.getWorkUnits().last();
        if (last == null || last.isFinished()) {
          WorkUnit workUnit = new WorkUnit(reloaded);
          persistentWork.persist(workUnit);
        }
      });
    });

    Duration time = task.getEstimatedTime();
    if (time == null || time.toMillis() == 0) {
      estimatedTimeBar.setProgress(-0.1D);
    } else {
      increaseProgress();

      long refreshRate = time.toMillis() / 100;
      refreshRate = Math.min(60 * 1000, refreshRate);
      log.debug("Triggering progress updates at a rate of {}ms", refreshRate);
      executorService.scheduleAtFixedRate(this::increaseProgress, 100, refreshRate, TimeUnit.MILLISECONDS);

      estimatedTime.setText(getHourMinutesString(time));
    }

    Task lastWorkedOnTask = window.getWorkingOnTaskLink().getCurrentTask();
    if (lastWorkedOnTask != null && !lastWorkedOnTask.equals(task)) {
      executorService.submit(() -> {
        persistentWork.run(session -> {
          Task old = persistentWork.reload(lastWorkedOnTask);
          if (old.getWorkUnits().size() > 0 && !old.getWorkUnits().last().isFinished()) {
            old.getWorkUnits().last().stop();
          }
        });
      });
    }
    window.getWorkingOnTaskLink().setCurrentTask(task);
  }

  @Override
  public void duringSave(Task model) {
    model.setDescription(description.getText());
  }

  public String getHourMinutesString(Duration duration) {
    long hours = duration.toHours();
    if (hours == 0) {
      return duration.toMinutes() + localized.get("duration.minutes");
    } else {
      long remainingMinutes = duration.minus(Duration.ofHours(hours)).toMinutes();
      return hours + ":" + String.format("%02d", remainingMinutes) + localized.get("duration.hours.short");
    }
  }

  private void increaseProgress() {
    Task task = store.getModel();
    Duration estimatedTime = task.getEstimatedTime();
    if (estimatedTime == null || estimatedTime.toMillis() == 0) {
      controller.getJavaFXExecutor().submit(() -> estimatedTimeBar.setProgress(-0.1D));
    } else {
      Task reloaded = persistentWork.read(session -> {
        Task reload = persistentWork.reload(task);
        reload.getWorkUnits().forEach(u -> u.getDuration());
        return reload;
      });

      Duration took = reloaded.getTotalWorkDuration();
      controller.getJavaFXExecutor().submit(() -> tookString.set(getHourMinutesString(took)));

      if (took.compareTo(estimatedTime) < 0) {
        double progress = 100D / Math.max(estimatedTime.toMillis(), 1) * took.toMillis() / 100D;
        controller.getJavaFXExecutor().submit(() -> estimatedTimeBar.setProgress(progress));
      } else {
        Duration over = took.minus(estimatedTime);
        controller.getJavaFXExecutor().submit(() -> estimatedTimeBar.setProgress(1.0));
        controller.getJavaFXExecutor().submit(() -> overTime.setText(getHourMinutesString(over)));
      }
    }
  }
}
