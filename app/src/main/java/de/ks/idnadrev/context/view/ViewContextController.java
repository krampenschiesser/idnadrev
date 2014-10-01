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
package de.ks.idnadrev.context.view;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.idnadrev.context.CreateContextActivity;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewContextController extends BaseController<List<Context>> {
  private static final String key_context = PropertyPath.property(Task.class, t -> t.getContext());
  @FXML
  protected ListView<Context> contextList;
  @FXML
  protected Button edit;
  @FXML
  protected Button create;
  @FXML
  protected Button delete;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    ReadOnlyBooleanProperty invalidProperty = validationRegistry.invalidProperty();
    BooleanBinding nothingSelected = contextList.getSelectionModel().selectedItemProperty().isNull();

    edit.disableProperty().bind(nothingSelected.or(invalidProperty));
    delete.disableProperty().bind(nothingSelected.or(invalidProperty));
    create.disableProperty().bind(invalidProperty);
  }

  @Override
  protected void onRefresh(List<Context> model) {
    contextList.setItems(FXCollections.observableArrayList(model));
  }

  @FXML
  void onCreate() {
    ActivityHint hint = new ActivityHint(CreateContextActivity.class, controller.getCurrentActivityId());
    controller.startOrResume(hint);
  }

  @FXML
  void onEdit() {
    Context item = contextList.getSelectionModel().getSelectedItem();

    ActivityHint hint = new ActivityHint(CreateContextActivity.class, controller.getCurrentActivityId());
    hint.setDataSourceHint(() -> item);

    controller.startOrResume(hint);
  }

  @FXML
  void onDelete() {
    Context item = contextList.getSelectionModel().getSelectedItem();
    store.executeCustomRunnable(() -> {
      PersistentWork.run(em -> {
        Context reload = PersistentWork.reload(item);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Task> update = builder.createCriteriaUpdate(Task.class);
        Root<Task> root = update.from(Task.class);
        Path<Context> contextPath = root.get(key_context);
        update.set(contextPath, builder.nullLiteral(Context.class));
        update.where(builder.equal(contextPath, reload));

        em.createQuery(update).executeUpdate();
        em.remove(reload);
      });
    });
    store.reload();
  }
}
