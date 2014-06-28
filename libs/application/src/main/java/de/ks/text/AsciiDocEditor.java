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
package de.ks.text;

import de.ks.activity.initialization.LoadInFXThread;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@LoadInFXThread
public class AsciiDocEditor implements Initializable {

  @FXML
  protected TextArea editor;
  @FXML
  protected WebView preview;
  @FXML
  protected Button help;
  @FXML
  protected Button save;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @FXML
  void insertMath() {

  }

  @FXML
  void insertImage() {

  }

  @FXML
  void saveToFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(save.getScene().getWindow());

  }

  @FXML
  void showHelp() {

  }

}
