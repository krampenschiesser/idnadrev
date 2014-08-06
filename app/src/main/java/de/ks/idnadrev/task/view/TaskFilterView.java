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

import de.ks.BaseController;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.TaskState;
import de.ks.reflection.PropertyPath;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class TaskFilterView extends BaseController<Void> {
  @FXML
  protected TextField description;
  @FXML
  protected ComboBox<TaskState> state;
  @FXML
  protected CheckBox showFinished;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    state.setItems(FXCollections.observableList(Arrays.asList(TaskState.NONE, TaskState.LATER, TaskState.ASAP, TaskState.DELEGATED)));
    state.setValue(TaskState.NONE);

    description.textProperty().addListener((p, o, n) -> triggerFilter());
    state.valueProperty().addListener((p, o, n) -> triggerFilter());
    showFinished.selectedProperty().addListener((p, o, n) -> triggerFilter());
  }

  private void triggerFilter() {
    controller.reload();
  }

  public void applyFilterOnDS(ViewTasksDS datasource) {
    datasource.setFilter((root, query, builder) -> {
      ArrayList<Predicate> predicates = new ArrayList<>();

      String descriptionText = description.textProperty().getValueSafe().trim();
      if (!descriptionText.isEmpty()) {
        Path<String> desc = root.get(PropertyPath.property(Task.class, t -> t.getDescription()));
        Predicate like = builder.like(builder.lower(desc), "%" + descriptionText + "%");
        predicates.add(like);
      }

      Path<String> statePath = root.get(PropertyPath.property(Task.class, t -> t.getState()));
      Predicate stateEquals = builder.equal(statePath, state.getValue());
      predicates.add(stateEquals);

      String finishTime = PropertyPath.property(Task.class, (t) -> t.getFinishTime());
      if (!showFinished.isSelected()) {
        predicates.add(root.get(finishTime).isNull());
      }
      query.where(predicates.toArray(new Predicate[predicates.size()]));
    });
  }

}
