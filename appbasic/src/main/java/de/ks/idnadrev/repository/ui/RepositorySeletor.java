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
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.initialization.ActivityCallback;
import de.ks.standbein.i18n.Localized;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RepositorySeletor implements Initializable, ActivityCallback {
  @FXML
  protected ComboBox<String> repository;

  @Inject
  RepositoryService repositoryService;
  @Inject
  ActivityController controller;
  @Inject
  Localized localized;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    //
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    controller.getJavaFXExecutor().submit(() -> {
      ObservableList<String> items = repository.getItems();
      items.clear();
      String all = localized.get("all");
      items.add(all);
      items.addAll(repositoryService.getRepositories().stream().map(Repository::getName).collect(Collectors.toList()));
      if (repository.getSelectionModel().isEmpty()) {
        repository.getSelectionModel().select(all);
      } else if (!items.contains(repository.getSelectionModel().getSelectedItem())) {
        repository.getSelectionModel().select(all);
      }
    });
  }

  public ComboBox<String> getRepositoryCombo() {
    return repository;
  }
}
