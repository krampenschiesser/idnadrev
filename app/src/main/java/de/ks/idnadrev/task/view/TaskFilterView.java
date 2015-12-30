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

import de.ks.flatjsondb.selection.NamedEntitySelection;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.BaseController;
import de.ks.standbein.javafx.event.ChainedEventHandler;
import de.ks.standbein.javafx.event.ClearTextOnEscape;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class TaskFilterView extends BaseController<Void> {
  @FXML
  protected TextField description;
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

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ChainedEventHandler<KeyEvent> clearOrHide = new ChainedEventHandler<>(new ClearTextOnEscape(), e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        description.getScene().getWindow().hide();
      }
    });
    description.setOnKeyReleased(clearOrHide);
    parentProjectController.configure(Task.class);
    parentProjectContainer.getChildren().add(parentProjectController.getRoot());
    parentProjectController.getTextField().setOnKeyReleased(clearOrHide);
    parentProjectController.itemProperty().addListener((p, o, n) -> triggerFilter());

    description.textProperty().addListener((p, o, n) -> triggerFilter());
    showAsap.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showLater.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showDelegated.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showFinished.selectedProperty().addListener((p, o, n) -> triggerFilter());
    showDefault.selectedProperty().addListener((p, o, n) -> triggerFilter());
  }

  // FIXME: 12/15/15 
//  public boolean needsToKeepFocus() {
//    return parentProjectController.isSelectingProject();
//  }

  private void triggerFilter() {
    controller.reload();
  }

  public void applyFilterOnDS(ViewTasksDS datasource) {
    // FIXME: 12/15/15 
//    datasource.setFilter((root, query, builder) -> {
//      ArrayList<Predicate> predicates = new ArrayList<>();
//
//      String descriptionText = description.textProperty().getValueSafe().trim();
//      if (!descriptionText.isEmpty()) {
//        Path<String> desc = root.get(PropertyPath.property(Task.class, t -> t.getDescription()));
//        Predicate like = builder.like(builder.lower(desc), "%" + descriptionText + "%");
//        predicates.add(like);
//      }
//
//      Path<String> statePath = root.get(PropertyPath.property(Task.class, t -> t.getState()));
//      ArrayList<Predicate> stateOrCombination = new ArrayList<>();
//
//      if (showDefault.isSelected()) {
//        stateOrCombination.add(builder.equal(statePath, TaskState.NONE));
//      }
//      if (showAsap.isSelected()) {
//        stateOrCombination.add(builder.equal(statePath, TaskState.ASAP));
//      }
//      if (showLater.isSelected()) {
//        stateOrCombination.add(builder.equal(statePath, TaskState.LATER));
//      }
//      if (showDelegated.isSelected()) {
//        stateOrCombination.add(builder.equal(statePath, TaskState.DELEGATED));
//      }
//      Predicate or = builder.or(stateOrCombination.toArray(new Predicate[stateOrCombination.size()]));
//      predicates.add(or);
//
//      String finishTime = PropertyPath.property(Task.class, (t) -> t.getFinishTime());
//      if (!showFinished.isSelected()) {
//        predicates.add(root.get(finishTime).isNull());
//      }
//
//      if (parentProjectController.getSelectedValue() != null) {
//        Predicate parentNotNull = root.get(PropertyPath.property(Task.class, t -> t.getParent())).isNotNull();
//        Predicate isParent = builder.equal(root.get("id"), parentProjectController.getSelectedValue().getId());
//        predicates.add(builder.or(parentNotNull, isParent));
//      }
//      query.where(predicates.toArray(new Predicate[predicates.size()]));
//    });
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

}
