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
package de.ks.idnadrev.repository.manage;

import de.ks.idnadrev.crud.CRUDController;
import de.ks.idnadrev.util.ButtonHelper;
import de.ks.standbein.BaseController;
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
  private TextField repositoryText;
  @FXML
  private Button select;
  @FXML
  private CRUDController crudController;

  @Inject
  ActivityContext context;
  @Inject
  ButtonHelper buttonHelper;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    validationRegistry.registerValidator(repositoryText, new FileExistsValidator(localized));
    validationRegistry.registerValidator(repositoryText, new NotEmptyValidator(localized));
    Button saveButton = crudController.getSaveButton();
    saveButton.setVisible(true);

    list.getItems().addListener((ListChangeListener<Path>) c -> {
      if (c.getList().isEmpty()) {
        crudController.backDisabledProperty().set(true);
        saveButton.setDisable(true);
      } else {
        crudController.backDisabledProperty().set(false);
        saveButton.setDisable(false);
      }
    });


    crudController.getDeleteButton().disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
    add.disableProperty().bind(validationRegistry.invalidProperty());
    buttonHelper.enhanceButton(add, "add.png", 24);
    buttonHelper.enhanceButton(select, "open.png", 24);


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
          }
        };
      }
    });
  }

  @FXML
  void onAdd() {
    ObservableList<Path> items = list.getItems();
    Path path = Paths.get(repositoryText.getText());
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
//      controller.stopCurrentStartNew(new ActivityHint(OverviewActivity.class));// FIXME: 4/10/16 
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
      crudController.backDisabledProperty().set(true);
      crudController.getSaveButton().setDisable(true);
    }
  }

  @Override
  public void duringSave(List<Path> model) {
    model.clear();
    model.addAll(list.getItems());
  }
}
