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
package de.ks.idnadrev.repository;

import de.ks.flatadocdb.Repository;
import de.ks.flatjsondb.RepositorySelector;
import de.ks.idnadrev.overview.OverviewActivity;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.standbein.activity.context.ActivityContext;
import de.ks.standbein.validation.validators.FileExistsValidator;
import de.ks.standbein.validation.validators.NotEmptyValidator;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RepositoryController extends BaseController<List<Path>> {
  @FXML
  private ListView<Path> list;
  @FXML
  private Button add;
  @FXML
  private Button save;
  @FXML
  private Button cancel;
  @FXML
  private Button deleteSelected;
  @FXML
  private TextField repositoryText;
  @FXML
  private Button select;

  @Inject
  protected RepositorySelector repositorySelector;
  @Inject
  ActivityContext context;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    validationRegistry.registerValidator(repositoryText, new FileExistsValidator(localized));
    validationRegistry.registerValidator(repositoryText, new NotEmptyValidator(localized));

    list.getItems().addListener((ListChangeListener<Path>) c -> {
      if (c.getList().isEmpty()) {
        cancel.setDisable(true);
        save.setDisable(true);
      } else {
        cancel.setDisable(false);
        save.setDisable(false);
      }
    });

    deleteSelected.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
    add.disableProperty().bind(validationRegistry.invalidProperty());


    list.setCellFactory(new Callback<ListView<Path>, ListCell<Path>>() {
      @Override
      public ListCell<Path> call(ListView<Path> param) {
        return new ListCell<Path>() {
          @Override
          protected void updateItem(Path item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
              setGraphic(new Label(item.toString()));
            } else {
              setGraphic(null);
            }
            if (repositorySelector.hasCurrentRepository()) {
              if (repositorySelector.getCurrentRepository().getPath().equals(item)) {
                getGraphic().setStyle("-fx-font-weight: bold;");
              }
            }
          }
        };
      }
    });
  }

  @FXML
  void onAdd() {
    ObservableList<Path> items = list.getItems();
    Path path = Paths.get(repositoryText.getText());
    if (items.isEmpty()) {
      repositorySelector.setCurrentRepository(new Repository(path));
    }
    if (!items.contains(path)) {
      items.add(path);
    }
  }

  @FXML
  void onSelect() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Open Resource File");

    File selectedFolder = chooser.showDialog(select.getScene().getWindow());
    if (selectedFolder != null) {
      repositoryText.setText(selectedFolder.getAbsolutePath());
      controller.getJavaFXExecutor().submit(() -> add.requestFocus());
    }
  }

  @FXML
  void onCancel() {
    stopAndReturn();
  }

  private void stopAndReturn() {
    if (context.isMainActivity(context.getCurrentActivity())) {
      controller.stopCurrentStartNew(new ActivityHint(OverviewActivity.class));
    } else {
      controller.stopCurrent();
    }
  }

  @FXML
  void onDeleteSelected() {
    List<Path> selectedItems = new ArrayList<>(list.getSelectionModel().getSelectedItems());
    list.getItems().removeAll(selectedItems);
  }

  @FXML
  void onSave() {
    store.save();
    stopAndReturn();
  }

  @Override
  protected void onRefresh(List<Path> model) {
    list.getItems().clear();
    list.getItems().addAll(model);
    if (model.isEmpty()) {
      cancel.setDisable(true);
      save.setDisable(true);
    }
  }

  @Override
  public void duringSave(List<Path> model) {
    model.clear();
    model.addAll(list.getItems());
  }
}
