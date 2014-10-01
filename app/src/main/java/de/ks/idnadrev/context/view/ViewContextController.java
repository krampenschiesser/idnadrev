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
package de.ks.idnadrev.context.view;

import de.ks.BaseController;
import de.ks.idnadrev.entity.Context;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewContextController extends BaseController<List<Context>> {
  @FXML
  protected ListView<Context> contextList;
  @FXML
  protected Button edit;
  @FXML
  protected Button create;
  @FXML
  protected Button delete;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @FXML
  void onCreate() {

  }

  @FXML
  void onEdit() {

  }

  @FXML
  void onDelete() {

  }
}
