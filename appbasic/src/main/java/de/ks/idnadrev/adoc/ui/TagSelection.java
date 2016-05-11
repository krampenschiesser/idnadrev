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
package de.ks.idnadrev.adoc.ui;

import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.standbein.autocomp.AutoCompletionTextField;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TagSelection implements Initializable {
  @FXML
  Button clear;
  @FXML
  FlowPane tagContainer;
  @FXML
  TextField textField;
  @Inject
  AutoCompletionTextField autoCompletionTextField;

  @Inject
  Index index;

  protected final ObservableList<String> selectedTags = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    autoCompletionTextField.configure(this::getItems, textField);
    autoCompletionTextField.setOnAction(e -> {
      String item = autoCompletionTextField.getItem();
      if (!selectedTags.contains(item)) {
        selectedTags.add(item);
      }
      autoCompletionTextField.getTextField().setText("");
    });

    selectedTags.addListener((ListChangeListener<String>) c -> {
      tagContainer.getChildren().clear();
      selectedTags.forEach(t -> {
        Label label = new Label(t);
        label.getStyleClass().add("tag");
        tagContainer.getChildren().add(label);
      });
    });
    clear.disableProperty().bind(Bindings.isEmpty(selectedTags));
  }

  private List<String> getItems(String input) {
    List<String> tags = index.queryValues(StandardQueries.byTagsQuery(), s -> !s.isEmpty()).stream().flatMap(Collection::stream).filter(tag -> tag.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    Collections.sort(tags);
    tags.removeAll(selectedTags);
    return tags;
  }

  public ObservableList<String> getSelectedTags() {
    return selectedTags;
  }

  @FXML
  public void onClear() {
    selectedTags.clear();
  }

  public void setEditable(boolean editable) {
    autoCompletionTextField.setEditable(editable);
  }
}
