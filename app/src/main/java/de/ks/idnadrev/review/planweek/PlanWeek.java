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
package de.ks.idnadrev.review.planweek;

import de.ks.BaseController;
import de.ks.fxcontrols.weekview.WeekView;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.task.view.ViewTasksMaster;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PlanWeek extends BaseController<List<Task>> {
  private static final Logger log = LoggerFactory.getLogger(PlanWeek.class);
  @FXML
  protected ViewTasksMaster viewController;
  @FXML
  protected StackPane weekViewContainer;

  private WeekView<Task> weekView;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    weekView = new WeekView<>(Localized.get("today"));
    weekView.setPrefSize(300, 300);
    weekViewContainer.getChildren().add(weekView);

    viewController.getTasksView().getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null) {
        Task task = n.getValue();
        WeekViewAppointment<Task> appointment = createAppointment(task);
        appointment.getControl().setVisible(false);
        weekView.getEntries().add(appointment);
      }
    });

    viewController.getTasksView().setOnDragDetected(e -> {
      TreeItem<Task> selectedItem = viewController.getTasksView().getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
        return;
      }
      Task task = selectedItem.getValue();

      WeekViewAppointment<Task> appointment = createAppointment(task);

      weekView.startAppointmentDrag(appointment);
      log.debug("Detected drag gesture, selected item={}", task.getName());
      e.consume();
    });
  }

  protected WeekViewAppointment<Task> createAppointment(Task task) {
    WeekViewAppointment<Task> appointment = new WeekViewAppointment<>(task.getName(), weekView.getFirstDayOfWeek(), task.getEstimatedTime());
    appointment.setUserData(task);
    appointment.setChangeStartCallback((date, time) -> log.info("{} now starting at {} {}", task.getName(), date, time));
    appointment.setNewTimePossiblePredicate((date, time) -> true);
    return appointment;
  }
}
