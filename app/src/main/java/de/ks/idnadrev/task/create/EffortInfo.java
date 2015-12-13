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
package de.ks.idnadrev.task.create;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

public class EffortInfo extends BaseController<Task> {

  @FXML
  protected Slider funFactor;
  @FXML
  protected Slider mentalEffort;
  @FXML
  protected Slider physicalEffort;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    physicalEffort.valueProperty().bindBidirectional(store.getBinding().getIntegerProperty(Task.class, (t) -> t.getPhysicalEffort().getAmount()));
    mentalEffort.valueProperty().bindBidirectional(store.getBinding().getIntegerProperty(Task.class, (t) -> t.getMentalEffort().getAmount()));
    funFactor.valueProperty().bindBidirectional(store.getBinding().getIntegerProperty(Task.class, (t) -> t.getFunFactor().getAmount()));
  }
}
