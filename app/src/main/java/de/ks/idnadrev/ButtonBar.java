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
package de.ks.idnadrev;

import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.idnadrev.cost.account.CreateAccountActivity;
import de.ks.idnadrev.cost.bookingview.BookingViewActivity;
import de.ks.idnadrev.cost.createbooking.CreateBookingActivity;
import de.ks.idnadrev.information.diary.DiaryActivity;
import de.ks.idnadrev.information.text.TextInfoActivity;
import de.ks.idnadrev.information.uml.UmlDiagramActivity;
import de.ks.idnadrev.information.view.InformationOverviewActivity;
import de.ks.idnadrev.overview.OverviewActivity;
import de.ks.idnadrev.review.planweek.PlanWeekActivity;
import de.ks.idnadrev.review.weeklydone.WeeklyDoneActivity;
import de.ks.idnadrev.task.choosenext.ChooseNextTaskActivity;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.task.fasttrack.FastTrackActivity;
import de.ks.idnadrev.task.view.ViewTasksActvity;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.idnadrev.thought.view.ViewThoughtsActivity;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class ButtonBar implements Initializable {
  @Inject
  ActivityController controller;

  @FXML
  GridPane root;
  private Popup popup;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    root.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.F1 || e.getCode() == KeyCode.ESCAPE) {
        hide();
        e.consume();
      }
    });
  }

  @FXML
  void overview() {
    controller.startOrResume(new ActivityHint(OverviewActivity.class));
    hide();
  }

  @FXML
  void addThought() {
    controller.startOrResume(new ActivityHint(AddThoughtActivity.class));
    hide();
  }

  @FXML
  void viewThoughts() {
    controller.startOrResume(new ActivityHint(ViewThoughtsActivity.class));
    hide();
  }

  @FXML
  void createTask() {
    controller.startOrResume(new ActivityHint(CreateTaskActivity.class));
    hide();
  }

  @FXML
  void viewTasks() {
    controller.startOrResume(new ActivityHint(ViewTasksActvity.class));
    hide();
  }

  @FXML
  void chooseNextTask() {
    controller.startOrResume(new ActivityHint(ChooseNextTaskActivity.class));
    hide();
  }

  @FXML
  void createTextInfo() {
    controller.startOrResume(new ActivityHint(TextInfoActivity.class));
    hide();
  }

  public void fastTrack() {
    controller.startOrResume(new ActivityHint(FastTrackActivity.class));
    hide();
  }

  @FXML
  public void weeklyDone() {
    controller.startOrResume(new ActivityHint(WeeklyDoneActivity.class));
    hide();
  }

  @FXML
  public void planWeek() {
    controller.startOrResume(new ActivityHint(PlanWeekActivity.class));
    hide();
  }

  @FXML
  public void informationOverview() {
    controller.startOrResume(new ActivityHint(InformationOverviewActivity.class));
    hide();
  }

  @FXML
  public void diary() {
    controller.startOrResume(new ActivityHint(DiaryActivity.class));
    hide();
  }

  @FXML
  public void umlDiagram() {
    controller.startOrResume(new ActivityHint(UmlDiagramActivity.class));
    hide();
  }

  @FXML
  public void bookingView() {
    controller.startOrResume(new ActivityHint(BookingViewActivity.class));
    hide();
  }

  @FXML
  public void createAccount() {
    controller.startOrResume(new ActivityHint(CreateAccountActivity.class));
    hide();
  }

  @FXML
  public void createBooking() {
    controller.startOrResume(new ActivityHint(CreateBookingActivity.class));
    hide();
  }

  public void show(Pane contentPane) {
    contentPane.getStyleClass().add("fadingContent");
    this.popup = new Popup();
    popup.getContent().add(root);
    popup.requestFocus();
    this.popup.show(contentPane.getScene().getWindow());
    popup.setOnHiding(e -> {
      contentPane.getStyleClass().remove("fadingContent");
      this.popup = null;
    });
  }

  public boolean isShowing() {
    return popup != null;
  }

  public void hide() {
    if (popup != null) {
      popup.hide();
      popup = null;
    }
  }
}
