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
package de.ks.idnadrev.task.view;

import de.ks.flatadocdb.session.Session;
import de.ks.flatjsondb.PersistentWork;
import de.ks.flatjsondb.selection.NamedEntitySelection;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.standbein.BaseController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TaskFilterView extends BaseController<Void> {
  @FXML
  protected ComboBox<String> contextSelection;
  @FXML
  protected TextField searchField;
  @FXML
  protected StackPane parentProjectContainer;
  @FXML
  protected CheckBox showAsap;
  @FXML
  protected CheckBox showDefault;
  @FXML
  protected CheckBox showLater;
  @FXML
  protected CheckBox showDelegated;
  @FXML
  protected CheckBox showFinished;

  @Inject
  NamedEntitySelection<Task> parentProjectController;
  @Inject
  PersistentWork persistentWork;
  private String contextKeyAll;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    parentProjectController.configure(Task.class);
    parentProjectContainer.getChildren().add(parentProjectController.getRoot());
    parentProjectController.itemProperty().addListener((p, o, n) -> triggerFilter());

    showAsap.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showLater.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showDelegated.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showFinished.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showDefault.selectedProperty().addListener((p, o, n) -> triggerFilter());

    contextSelection.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> triggerFilter());
//    searchField.textProperty().addListener((p, o, n) -> triggerFilter());

    contextKeyAll = localized.get("all");
  }

  // FIXME: 12/15/15 
//  public boolean needsToKeepFocus() {
//    return parentProjectController.isSelectingProject();
//  }

  private void triggerFilter() {
    controller.reload();
  }

  public void applyFilterOnDS(Session.MultiQueyBuilder<Task> builder) {
    builder.query(Task.finishedQuery(), finished -> showFinished.isSelected() == finished);

    builder.query(Task.stateQuery(), state -> {
      boolean valid = state == TaskState.ASAP && showAsap.isSelected();
      valid = valid || state == TaskState.DELEGATED && showDelegated.isSelected();
      valid = valid || state == TaskState.LATER && showLater.isSelected();
      valid = valid || state == TaskState.NONE && showDefault.isSelected();
      return valid;
    });

    String contextString = contextSelection.getSelectionModel().getSelectedItem();
    if (contextString != null && !contextString.isEmpty()) {
      if (!contextString.equals(contextKeyAll)) {
        builder.query(Task.contextNameQuery(), contextString::equals);
      }
    } else {
      builder.query(Task.contextNameQuery(), Objects::isNull);
    }
  }

  public String getSearchContent() {
    return searchField.textProperty().getValueSafe();
  }

  public Task getParentTask() {
    // FIXME: 12/15/15 
//    String parentProjectName = parentProjectController.getInput().textProperty().getValueSafe().trim();
//    if (parentProjectController.getSelectedValue() == null && !parentProjectName.isEmpty()) {
//      return persistentWork.forName(Task.class, parentProjectName);
//    }
//    return parentProjectController.getSelectedValue();
    return null;
  }

  public int getMaxResults() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    CompletableFuture.supplyAsync(() -> persistentWork.from(Context.class).stream().map(c -> c.getName()).collect(Collectors.toList()), controller.getExecutorService())//
      .thenAcceptAsync(contextNames -> {
        ObservableList<String> items = FXCollections.observableArrayList(contextNames);
        items.add(0, "");
        items.add(1, contextKeyAll);
        contextSelection.setItems(items);
        contextSelection.getSelectionModel().select(1);
      }, controller.getJavaFXExecutor());
  }

}
