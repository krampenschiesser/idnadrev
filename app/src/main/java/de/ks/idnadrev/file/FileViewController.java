/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.idnadrev.file;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.idnadrev.entity.FileContainer;
import de.ks.standbein.activity.context.ActivityStore;
import de.ks.standbein.activity.initialization.DatasourceCallback;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileViewController implements Initializable, DatasourceCallback<FileContainer<?>> {
  private static final Logger log = LoggerFactory.getLogger(FileViewController.class);
  protected final ObservableList<File> files = FXCollections.observableArrayList();
  protected final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
  protected final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("awt.Desktop-%d").build());
  @FXML
  protected Button edit;
  @FXML
  protected Button open;
  @FXML
  protected Button openFolder;
  @FXML
  protected Button addNewFile;
  @FXML
  protected Button removeFile;
  @FXML
  protected Label fileNameLabel;
  @FXML
  protected Label folderName;
  @FXML
  protected ListView<File> fileList;

  @Inject
  protected ActivityStore store;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fileList.setItems(files);

    MultipleSelectionModel<File> selectionModel = fileList.getSelectionModel();
    selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

    ReadOnlyObjectProperty<File> selection = selectionModel.selectedItemProperty();
    selection.addListener((p, o, n) -> {
      folderName.setText(n == null ? "" : n.getParentFile().getAbsolutePath());
      fileNameLabel.setText(n == null ? "" : n.getName());
    });

    BooleanBinding isDirectory = Bindings.createBooleanBinding(() -> selection.get() != null && selection.get().isDirectory(), selection);
    edit.disableProperty().bind(isDirectory);

    BooleanBinding disable = fileList.getSelectionModel().selectedItemProperty().isNull();
    open.disableProperty().bind(disable);
    edit.disableProperty().bind(disable);
    openFolder.disableProperty().bind(disable);
    removeFile.disableProperty().bind(disable);
  }

  public void addFiles(List<File> additionalFiles) {
    additionalFiles.removeAll(files);
    additionalFiles.forEach(this::addPossibleImage);
    log.info("Adding addtional files {}", additionalFiles);
    files.addAll(additionalFiles);

    if (!additionalFiles.isEmpty()) {
      Collections.sort(files);
      Collections.sort(additionalFiles);
      File lastFile = additionalFiles.get(additionalFiles.size() - 1);
      fileList.scrollTo(lastFile);
      fileList.getSelectionModel().clearSelection();
      fileList.getSelectionModel().select(lastFile);
    }
  }

  protected void addPossibleImage(File file) {
    if (file == null) {
      return;
    }
    try {
      String contentType = Files.probeContentType(file.toPath());
      if (contentType != null && contentType.contains("image")) {
//        imageProvider.addImage(new ImageData(file.getName(), file.getPath()));
      }
    } catch (IOException e) {
      //
    }
  }

  public ObservableList<File> getFiles() {
    return files;
  }

  public ListView<File> getFileList() {
    return fileList;
  }

  @FXML
  void open() {
    ObservableList<File> items = fileList.getSelectionModel().getSelectedItems();
    for (File item : items) {
      executor.submit(() -> {
        try {
          log.info("Opening {}", item);
          desktop.open(item);
        } catch (IOException e) {
          log.error("Could not open {}", item, e);
        }
      });
    }
  }

  @FXML
  void edit() {
    ObservableList<File> items = fileList.getSelectionModel().getSelectedItems();
    for (File item : items) {
      executor.submit(() -> {
        try {
          log.info("Editing {}", item);
          desktop.edit(item);
        } catch (IOException e) {
          log.error("Could not open {}", item, e);
        }
      });
    }
  }

  @FXML
  void openFolder() {
    TreeSet<File> files = new TreeSet<>();

    ObservableList<File> items = fileList.getSelectionModel().getSelectedItems();
    for (File item : items) {
      if (item.isDirectory()) {
        files.add(item);
      } else {
        files.add(item.getParentFile());
      }
    }
    for (File file : files) {
      executor.submit(() -> {
        try {
          log.info("Opening {}", file);
          desktop.open(file);
        } catch (IOException e) {
          log.error("Could not open {}", file, e);
        }
      });
    }
  }

  @FXML
  public void removeFile() {
    ObservableList<File> selectedItems = fileList.getSelectionModel().getSelectedItems();
    log.info("Removing files {}", selectedItems);
    selectedItems.forEach(files::remove);
  }

  @FXML
  void addNewFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(edit.getScene().getWindow());
    if (file != null) {
      addFiles(Collections.singletonList(file));
    }
  }

  @Subscribe
  public void onRefresh() {
    log.debug("Clearing files");
    files.clear();
  }

  @Override
  public void duringLoad(FileContainer<?> model) {
    //nope
  }

  @Override
  public void duringSave(FileContainer<?> model) {
    if (this.files.isEmpty()) {
      log.info("No files to save for {}", model);
    }
    for (File file : files) {
      model.addFile(file.toPath());
    }
  }
}
