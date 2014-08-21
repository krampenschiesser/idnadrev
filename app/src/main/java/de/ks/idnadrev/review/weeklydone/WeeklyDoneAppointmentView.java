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

import de.ks.BaseController;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.idnadrev.entity.Task;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WeeklyDoneAppointmentView extends BaseController<List<WeekViewAppointment>> {
  protected final SimpleObjectProperty<WeekViewAppointment<Task>> appointment = new SimpleObjectProperty<>();

  @FXML
  private Label name;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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
  }

  private void clear() {
    name.setText("");
  }
}
