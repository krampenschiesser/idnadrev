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
package de.ks.idnadrev.task.work;

import de.ks.activity.ActivityController;
import de.ks.activity.initialization.LoadInFXThread;
import de.ks.text.AsciiDocParser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebView;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

@LoadInFXThread
public class WorkOnTask implements Initializable {
  @FXML
  protected Label estimatedTime;
  @FXML
  protected ProgressBar estimatedTimeBar;
  @FXML
  protected Label name;
  @FXML
  protected Label overTime;
  @FXML
  protected WebView description;

  @Inject
  protected ActivityController controller;
  @Inject
  protected AsciiDocParser parser;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }
}
