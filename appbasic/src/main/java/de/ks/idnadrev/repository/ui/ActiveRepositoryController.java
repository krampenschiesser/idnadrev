/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.repository.ui;

import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.repository.RepositoryService;
import de.ks.idnadrev.util.NamedConverter;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.initialization.ActivityCallback;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class ActiveRepositoryController implements Initializable, ActivityCallback {
  @FXML
  protected ComboBox<Repository> repository;

  @Inject
  RepositoryService repositoryService;
  @Inject
  ActivityController controller;

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    repository.setConverter(new NamedConverter<>(name -> repositoryService.getRepositories().stream()//
      .filter(r -> r.getName().equals(name)).findAny().get()));
    repository.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null) {
        repositoryService.setActiveRepository(n);
      }
    });
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    controller.getJavaFXExecutor().submit(() -> {
      ObservableList<Repository> items = repository.getItems();
      items.clear();
      items.addAll(repositoryService.getRepositories());
      if (repository.getSelectionModel().isEmpty() && !items.isEmpty()) {
        repository.getSelectionModel().select(repositoryService.getActiveRepository());
      } else if (!items.contains(repository.getSelectionModel().getSelectedItem())) {
        repository.getSelectionModel().select(repositoryService.getActiveRepository());
      }
    });
  }

  public ComboBox<Repository> getRepositoryCombo() {
    return repository;
  }
}
