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
package de.ks.activity.controllerbinding;

import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityStore;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class TestBindingController implements Initializable {
  @FXML
  private TextField name;
  @FXML
  private TextField jsonString;
  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;

  @FXML
  void save() {
    controller.save();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    name.textProperty().bindBidirectional(store.getBinding().getStringProperty(Option.class, Option::getName));
    jsonString.textProperty().bindBidirectional(store.getBinding().getStringProperty(Option.class, Option::getJSONString));
  }
}
