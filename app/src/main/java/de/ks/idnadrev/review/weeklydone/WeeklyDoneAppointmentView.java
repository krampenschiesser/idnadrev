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
package de.ks.idnadrev.review.weeklydone;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WeeklyDoneAppointmentView extends BaseController<List<WeekViewAppointment<Task>>> {
  protected final SimpleObjectProperty<WeekViewAppointment<Task>> appointment = new SimpleObjectProperty<>();

  @FXML
  Label name;
  @FXML
  Label startTime;
  @FXML
  Label endTime;
  @FXML
  Label duration;
  @FXML
  StackPane descriptionContainer;
  @FXML
  ImageView doneView;

  AsciiDocViewer viewer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CompletableFuture<DefaultLoader<Node, AsciiDocViewer>> future = activityInitialization.loadAdditionalControllerWithFuture(AsciiDocViewer.class);
    future.thenAcceptAsync(loader -> {
      descriptionContainer.getChildren().add(loader.getView());
      viewer = loader.getController();
    }, controller.getJavaFXExecutor());

    appointment.addListener((p, o, n) -> {
      clear();
      if (n != null) {
        applyContent(n);
      }
    });
    clear();
  }

  private void applyContent(WeekViewAppointment<Task> appointment) {
    name.setText(appointment.getTitle());
    startTime.setText(appointment.getStart().format(DateTimeFormatter.ISO_LOCAL_TIME));
    duration.setText(appointment.getDuration().toMinutes() + "min");
    endTime.setText(appointment.getEnd().format(DateTimeFormatter.ISO_LOCAL_TIME));
    viewer.show(new AsciiDocContent(appointment.getTitle(), appointment.getUserData().getDescription()));
    Button btn = (Button) appointment.getControl();
    Node graphic = btn.getGraphic();
    if (graphic instanceof ImageView) {
      Image image = ((ImageView) graphic).getImage();
      doneView.setImage(image);
    }
  }

  private void clear() {
    name.setText("");
    startTime.setText("");
    duration.setText("");
    endTime.setText("");
    doneView.setImage(null);
  }

  @Override
  protected void onRefresh(List<WeekViewAppointment<Task>> model) {
    List<AsciiDocContent> asciiDocContents = model.stream().map(appointment -> new AsciiDocContent(appointment.getTitle(), appointment.getUserData().getDescription())).collect(Collectors.toList());
    viewer.preload(asciiDocContents);
  }
}
