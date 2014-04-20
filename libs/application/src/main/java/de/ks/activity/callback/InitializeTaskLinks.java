/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.activity.callback;


import de.ks.activity.Activity;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.link.TaskLink;
import de.ks.application.fxml.LoaderCallback;
import de.ks.executor.ExecutorService;
import de.ks.executor.fx.ContextualEventHandler;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.List;
import java.util.function.Function;

/**
 *
 */
public class InitializeTaskLinks extends LoaderCallback {
  private static final Logger log = LoggerFactory.getLogger(InitializeTaskLinks.class);
  private final List<TaskLink> taskLinks;
  private final Activity activity;
  private final ActivityController activityController;
  private final ExecutorService service = CDI.current().select(ExecutorService.class).get();

  public InitializeTaskLinks(List<TaskLink> taskLinks, Activity activity, ActivityController activityController) {
    this.taskLinks = taskLinks;
    this.activity = activity;
    this.activityController = activityController;
  }

  @Override
  public void accept(Object controller, Node node) {
    log.debug("initializing task-links for controller {}", controller);
    taskLinks.stream().filter(taskLink -> taskLink.getSourceController().equals(controller.getClass())).forEach(taskLink -> {
      EventHandler<ActionEvent> handler = new ContextualEventHandler<>(actionEvent -> {
        Task<?> task = CDI.current().select(taskLink.getTask()).get();
        if (taskLink.isEnd()) {
          Function returnConverter = activity.getReturnConverter();
          task.setOnSucceeded(new ContextualEventHandler<>((e) -> {
                    if (returnConverter != null) {
                      @SuppressWarnings("unchecked") Object hint = returnConverter.apply(CDI.current().select(ActivityStore.class).get().getModel());
                      activityController.resumePreviousActivity(hint);
                    } else {
                      activityController.resumePreviousActivity();
                    }
                  }
                  )
          );
        }
        service.submit(task);
      });
      addHandlerToNode(node, taskLink.getId(), handler);
      log.debug("done with task-link {} for controller {}", taskLink.getId(), controller);
    });
  }
}
