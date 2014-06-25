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
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import de.ks.text.AsciiDocParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@LoadInFXThread
@ModelBound(Task.class)
public class WorkOnTask implements Initializable {
  @FXML
  protected Label estimatedTime;
  @FXML
  protected ProgressBar estimatedTimeBar;
  @FXML
  protected Label name;
  @FXML
  protected Label overTime;
  @FXML
  protected WebView description;

  @Inject
  protected ActivityController controller;
  @Inject
  protected ActivityStore store;
  @Inject
  protected AsciiDocParser parser;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

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
    description.getEngine().loadContent(task.getDescription());

    SuspendablePooledExecutorService executorService = controller.getCurrentExecutorService();

    CompletableFuture.supplyAsync(() -> parser.parse(task.getDescription()), executorService)//
            .thenAcceptAsync(html -> description.getEngine().loadContent(html), controller.getJavaFXExecutor());

    PersistentWork.runAsync(em -> {
      Task reloaded = PersistentWork.byId(Task.class, task.getId());
      WorkUnit workUnit = new WorkUnit(reloaded);
      em.persist(workUnit);
    }, executorService);
  }
}
