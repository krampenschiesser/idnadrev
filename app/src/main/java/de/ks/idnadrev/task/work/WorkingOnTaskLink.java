/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.task.work;

import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Task;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Hyperlink;

import javax.enterprise.inject.spi.CDI;

public class WorkingOnTaskLink extends Hyperlink {
  protected final SimpleObjectProperty<Task> currentTask = new SimpleObjectProperty<>();

  public WorkingOnTaskLink() {
    setVisible(false);
    currentTask.addListener((p, o, n) -> {
      if (n == null) {
        setText("");
        setVisible(false);
      } else {
        String taskName = n.getName().length() < 50 ? n.getName() : n.getName().substring(0, 50);
        String title = Localized.get("task.workingOn:") + " " + taskName;
        setText(title);
        setVisible(true);
      }
    });
    setOnAction(e -> workOnTask());
  }

  protected void workOnTask() {
    if (currentTask.get() != null) {
      ActivityController activityController = CDI.current().select(ActivityController.class).get();
      ActivityHint activityHint = new ActivityHint(WorkOnTaskActivity.class, activityController.getCurrentActivityId());
      activityHint.setDataSourceHint(() -> currentTask.get());
      activityController.startOrResume(activityHint);
    }
  }

  public Task getCurrentTask() {
    return currentTask.get();
  }

  public SimpleObjectProperty<Task> currentTaskProperty() {
    return currentTask;
  }

  public void setCurrentTask(Task currentTask) {
    this.currentTask.set(currentTask);
  }
}


