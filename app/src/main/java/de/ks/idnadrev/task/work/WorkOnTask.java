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

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.ModelBound;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.initialization.LoadInFXThread;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.executor.SuspendablePooledExecutorService;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import de.ks.text.AsciiDocParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@LoadInFXThread
@ModelBound(Task.class)
public class WorkOnTask implements Initializable {
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
  protected WebView descriptionView;
  @FXML
  protected TextArea description;
  @FXML
  protected Tab previewTab;
  @FXML
  protected Tab editTab;
  @FXML
  protected TabPane tabView;

  @Inject
  protected ActivityController controller;
  @Inject
  protected ActivityStore store;
  @Inject
  protected AsciiDocParser parser;
  private ScheduledFuture<?> progressFuture;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    description.textProperty().addListener((p, o, n) -> {
      if (n != null) {
        renderAsciiDoc(n, false);//FIXME use blocking queue last render pattern here
      }
    });
    overTime.textProperty().isNotEmpty().addListener((p, o, n) -> {
      if (n) {
        log.info("Switching to negative style class");
        estimatedTimeBar.getStyleClass().add(OVERTIME_STYLE_CLASS);
      } else {
        log.info("Switching to positive style class");
        estimatedTimeBar.getStyleClass().remove(OVERTIME_STYLE_CLASS);
      }
    });
  }

  @FXML
  void stopWork() {
    PersistentWork.run(em -> {
      Task task = store.getModel();
      task = PersistentWork.byId(Task.class, task.getId());
      task.getWorkUnits().last().stop();
    });
    controller.resumePreviousActivity();
  }

  @FXML
  void finishTask() {

  }

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void afterLoad(ActivityLoadFinishedEvent event) {
    Task task = event.getModel();
    String descriptionContent = task.getDescription();
    descriptionView.getEngine().loadContent(descriptionContent);

    SuspendablePooledExecutorService executorService = controller.getCurrentExecutorService();

    renderAsciiDoc(descriptionContent, true);

    PersistentWork.runAsync(em -> {
      Task reloaded = PersistentWork.byId(Task.class, task.getId());
      WorkUnit workUnit = new WorkUnit(reloaded);
      em.persist(workUnit);
    }, executorService);

    Duration time = task.getEstimatedTime();
    if (time == null || time.toMillis() == 0) {
      estimatedTimeBar.setProgress(-0.1D);
    } else {
      increaseProgress();

      long refreshRate = time.toMillis() / 100;
      refreshRate = Math.min(60 * 1000, refreshRate);
      log.debug("Triggering progress updates at a rate of {}ms", refreshRate);
      progressFuture = executorService.scheduleAtFixedRate(this::increaseProgress, 100, refreshRate, TimeUnit.MILLISECONDS);

      estimatedTime.setText(getHourMinutesString(time));
    }

  }

  public String getHourMinutesString(Duration duration) {
    long hours = duration.toHours();
    if (hours == 0) {
      return duration.toMinutes() + Localized.get("duration.minutes");
    } else {
      long remainingMinutes = duration.minus(Duration.ofHours(hours)).toMinutes();
      return hours + ":" + String.format("%02d", remainingMinutes) + Localized.get("duration.hours.short");
    }
  }

  public void renderAsciiDoc(String descriptionContent, boolean showTab) {
    CompletableFuture.supplyAsync(() -> parser.parse(descriptionContent), controller.getCurrentExecutorService())//
            .thenAcceptAsync(html -> {
              descriptionView.getEngine().loadContent(html);
              if (showTab && descriptionContent != null && !descriptionContent.isEmpty()) {
                tabView.getSelectionModel().select(previewTab);
              }
            }, controller.getJavaFXExecutor());
  }

  private void increaseProgress() {
    Task task = store.getModel();
    Duration estimatedTime = task.getEstimatedTime();
    if (estimatedTime == null || estimatedTime.toMillis() == 0) {
      Platform.runLater(() -> estimatedTimeBar.setProgress(-0.1D));
    } else {
      Task reloaded = PersistentWork.from(Task.class, (r, q, b) -> {
        q.where(b.equal(r.get("id"), task.getId()));
      }, (t) -> t.getWorkUnits().forEach(u -> u.getStart())).get(0);

      Duration took = reloaded.getTotalWorkDuration();

      if (took.compareTo(estimatedTime) < 0) {
        double progress = 100D / Math.max(estimatedTime.toMillis(), 1) * took.toMillis() / 100D;
        Platform.runLater(() -> estimatedTimeBar.setProgress(progress));
      } else {
        Duration over = took.minus(estimatedTime);

        Platform.runLater(() -> overTime.setText(getHourMinutesString(over)));
      }
    }
  }
}
