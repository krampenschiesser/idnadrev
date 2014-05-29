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

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.activity.ActivityLoadFinishedEvent;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileThoughtViewController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(FileThoughtViewController.class);
  protected final ObservableList<File> files = FXCollections.observableArrayList();
  protected final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
  protected final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("awt.Desktop-%d").build());
  @FXML
  private Button edit;
  @FXML
  private Label fileNameLabel;
  @FXML
  private Label folderName;
  @FXML
  private ListView<File> fileList;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fileList.setItems(files);

    MultipleSelectionModel<File> selectionModel = fileList.getSelectionModel();

    ReadOnlyObjectProperty<File> selection = selectionModel.selectedItemProperty();
    selection.addListener((p, o, n) -> {
      folderName.setText(n == null ? "" : n.getParentFile().getAbsolutePath());
      fileNameLabel.setText(n == null ? "" : n.getName());

    });

    BooleanBinding isDirectory = Bindings.createBooleanBinding(() -> selection.get() != null && selection.get().isDirectory(), selection);
    edit.disableProperty().bind(isDirectory);
  }


  public void addFiles(List<File> additionalFiles) {
    additionalFiles.removeAll(files);
    files.addAll(additionalFiles);
  }

  public ObservableList<File> getFiles() {
    return files;
  }

  @FXML
  void open(ActionEvent event) {
    File item = fileList.getSelectionModel().getSelectedItem();
    if (item == null) {
      return;
    }
    executor.submit(() -> {
      try {
        log.info("Opening {}", item);
        desktop.open(item);
      } catch (IOException e) {
        log.error("Could not open {}", item, e);
      }
    });
  }

  @FXML
  void edit(ActionEvent event) {
    File item = fileList.getSelectionModel().getSelectedItem();
    if (item == null) {
      return;
    }
    executor.submit(() -> {
      try {
        log.info("Editing {}", item);
        desktop.edit(item);
      } catch (IOException e) {
        log.error("Could not open {}", item, e);
      }
    });
  }

  @FXML
  void openFolder(ActionEvent event) {
    File item = fileList.getSelectionModel().getSelectedItem();
    if (item == null) {
      return;
    }
    if (item.isDirectory()) {
      executor.submit(() -> {
        try {
          log.info("Opening {}", item);
          desktop.open(item);
        } catch (IOException e) {
          log.error("Could not open {}", item, e);
        }
      });
    } else {
      File parentFile = item.getParentFile();

      executor.submit(() -> {
        try {
          log.info("Opening {}", parentFile);
          desktop.open(parentFile);
        } catch (IOException e) {
          log.error("Could not open {}", parentFile, e);
        }
      });
    }
  }

  @FXML
  void removeFile(ActionEvent event) {
    ObservableList<File> selectedItems = fileList.getSelectionModel().getSelectedItems();
    files.removeAll(selectedItems);
  }

  @FXML
  void addNewFile(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(edit.getScene().getWindow());
    if (file != null) {
      files.add(file);
    }
  }

  @Subscribe
  public void onRefresh(ActivityLoadFinishedEvent event) {
    files.clear();
  }
}
