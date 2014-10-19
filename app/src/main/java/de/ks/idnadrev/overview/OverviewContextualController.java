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
package de.ks.idnadrev.overview;

import de.ks.BaseController;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class OverviewContextualController extends BaseController<OverviewModel> {
  private static final Logger log = LoggerFactory.getLogger(OverviewContextualController.class);
  @FXML
  protected TableView<?> contextTasks;
  @FXML
  protected TableColumn<?, ?> estimatedTime;
  @FXML
  protected ComboBox<?> context;
  @FXML
  protected TableColumn<?, ?> name;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    name.prefWidthProperty().bind(contextTasks.widthProperty().subtract(estimatedTime.widthProperty()));
  }
}
