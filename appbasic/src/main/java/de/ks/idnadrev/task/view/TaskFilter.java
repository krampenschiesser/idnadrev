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
package de.ks.idnadrev.task.view;

import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.idnadrev.repository.ui.RepositorySeletor;
import de.ks.idnadrev.task.TaskState;
import de.ks.standbein.BaseController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskFilter extends BaseController<Object> {

  @FXML
  TextField title;
  @FXML
  TextField tags;
  @FXML
  ComboBox<String> context;
  @FXML
  RepositorySeletor repositoryController;
  @FXML
  ComboBox<String> state;

  @Inject
  Index index;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    state.getItems().add(localized.get("all"));
    state.getItems().addAll(Stream.of(TaskState.values()).map(TaskState::name).collect(Collectors.toList()));
    state.getSelectionModel().select(0);
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  protected void onRefresh(Object model) {
    List<String> contexts = index.queryValues(StandardQueries.contextQuery(), s -> true).stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    Collections.sort(contexts);
    ObservableList<String> items = context.getItems();
    items.clear();
    items.addAll(localized.get("all"));
    items.addAll(contexts);
    context.getSelectionModel().select(0);
  }
}
