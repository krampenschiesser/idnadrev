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
import de.ks.fxcontrols.weekview.AppointmentResolver;
import de.ks.fxcontrols.weekview.WeekView;
import de.ks.fxcontrols.weekview.WeekViewAppointment;
import de.ks.i18n.Localized;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class WeeklyDone extends BaseController<List<WeekViewAppointment>> implements AppointmentResolver {

  @FXML
  private StackPane weekContainer;
  protected WeekView weekView;
  private Consumer<List<WeekViewAppointment>> consumer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    weekView = new WeekView(Localized.get("today"));
    weekContainer.getChildren().add(weekView);
    controller.getJavaFXExecutor().execute(() -> weekView.setAppointmentResolver(this));
  }

  @Override
  public void resolve(LocalDate begin, LocalDate end, Consumer<List<WeekViewAppointment>> consumer) {
    this.consumer = consumer;
    WeeklyDoneDS datasource = (WeeklyDoneDS) store.getDatasource();
    datasource.beginDate = LocalDateTime.of(begin, LocalTime.of(0, 0));
    datasource.endDate = LocalDateTime.of(end, LocalTime.of(23, 59));
    controller.reload();
  }

  @Override
  protected void onRefresh(List<WeekViewAppointment> model) {
    if (consumer != null) {
      consumer.accept(model);
    }
  }
}
