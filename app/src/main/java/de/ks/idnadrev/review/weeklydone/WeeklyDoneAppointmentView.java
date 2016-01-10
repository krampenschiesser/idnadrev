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

import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.standbein.BaseController;
import de.ks.texteditor.preview.TextPreview;
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

  TextPreview viewer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextPreview.load(activityInitialization, view -> descriptionContainer.getChildren().add(view), controller -> viewer = controller);

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
    viewer.show(appointment.getUserData().getAdocFile().getTmpFile());
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
    model.forEach(t -> {
      AdocFile adocFile = t.getUserData().getAdocFile();
      viewer.preload(adocFile.getTmpFile(), adocFile.getRenderingPath(), adocFile.getContent());
    });
  }
}
