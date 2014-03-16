package de.ks.activity.callback;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.ActivityController;
import de.ks.activity.link.TaskLink;
import de.ks.executor.ExecutorService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.List;

/**
 *
 */
public class InitializeTaskLinks extends LoaderCallback {
  private static final Logger log = LoggerFactory.getLogger(InitializeTaskLinks.class);
  private final List<TaskLink> taskLinks;
  private final ActivityController activityController;

  public InitializeTaskLinks(List<TaskLink> taskLinks, ActivityController activityController) {
    this.taskLinks = taskLinks;
    this.activityController = activityController;
  }

  @Override
  public void accept(Object controller, Node node) {
    log.debug("initializing task-links for controller {}", controller);
    for (TaskLink taskLink : taskLinks) {
      if (taskLink.getSourceController().equals(controller.getClass())) {
        EventHandler<ActionEvent> handler = actionEvent -> {
          Task<?> task = CDI.current().select(taskLink.getTask()).get();
          ExecutorService.instance.submit(task);
        };
        addHandlerToNode(node, taskLink.getId(), handler);
        log.debug("done with task-link {} for controller {}", taskLink.getId(), controller);
      }
    }

  }
}
