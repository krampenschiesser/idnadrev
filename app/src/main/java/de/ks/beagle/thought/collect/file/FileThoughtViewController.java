/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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
package de.ks.beagle.thought.collect.file;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FileThoughtViewController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(FileThoughtViewController.class);
  protected final ObservableList<File> files = FXCollections.observableArrayList();

  @FXML
  private Label fileNameLabel;

  @FXML
  private ListView<File> fileList;


  public void addFiles(List<File> additionalFiles) {
    additionalFiles.removeAll(files);
    files.addAll(additionalFiles);
  }

  public ObservableList<File> getFiles() {
    return files;
  }

  @FXML
  void preview(ActionEvent event) {

  }

  @FXML
  void edit(ActionEvent event) {

  }

  @FXML
  void openFolder(ActionEvent event) {

  }

  @FXML
  void removeFile(ActionEvent event) {

  }

  @FXML
  void addNewFile(ActionEvent event) {

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fileList.setItems(files);

    ReadOnlyObjectProperty<File> selection = fileList.getSelectionModel().selectedItemProperty();
    selection.addListener((p, o, n) -> fileNameLabel.setText(n.getName()));
  }
}
