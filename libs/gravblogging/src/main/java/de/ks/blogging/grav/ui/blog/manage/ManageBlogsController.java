/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.ui.blog.manage;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.ui.blog.edit.CreateEditBlogActivity;
import de.ks.fxcontrols.cell.ConvertingListCell;
import de.ks.persistence.PersistentWork;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ManageBlogsController extends BaseController<List<GravBlog>> {

  @FXML
  protected ListView<GravBlog> blogList;
  @FXML
  protected Button edit;
  @FXML
  protected Button create;
  @FXML
  protected Button delete;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    ReadOnlyBooleanProperty invalidProperty = validationRegistry.invalidProperty();
    BooleanBinding nothingSelected = blogList.getSelectionModel().selectedItemProperty().isNull();

    edit.disableProperty().bind(nothingSelected.or(invalidProperty));
    delete.disableProperty().bind(nothingSelected.or(invalidProperty));
    create.disableProperty().bind(invalidProperty);

    blogList.setCellFactory(v -> new ConvertingListCell<GravBlog>(c -> c.getName()));
    blogList.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        onEdit();
        e.consume();
      }
      if (e.getCode() == KeyCode.DELETE) {
        onDelete();
        e.consume();
      }
    });
  }

  @Override
  protected void onRefresh(List<GravBlog> model) {
    int selectedIndex = blogList.getSelectionModel().getSelectedIndex();
    selectedIndex = selectedIndex == -1 ? 0 : selectedIndex;
    if (selectedIndex > model.size()) {
      selectedIndex = 0;
    }

    final int select = selectedIndex;
    blogList.setItems(FXCollections.observableArrayList(model));
    if (!model.isEmpty()) {
      controller.getJavaFXExecutor().submit(() -> blogList.getSelectionModel().select(select));
    }
  }

  @FXML
  public void onCreate() {
    ActivityHint hint = new ActivityHint(CreateEditBlogActivity.class, controller.getCurrentActivityId());
    hint.setDataSourceHint(() -> null);
    controller.startOrResume(hint);
  }

  @FXML
  public void onEdit() {
    GravBlog item = blogList.getSelectionModel().getSelectedItem();

    ActivityHint hint = new ActivityHint(CreateEditBlogActivity.class, controller.getCurrentActivityId());
    hint.setDataSourceHint(() -> item);

    controller.startOrResume(hint);
  }

  @FXML
  public void onDelete() {
    GravBlog item = blogList.getSelectionModel().getSelectedItem();
    store.executeCustomRunnable(() -> {
      PersistentWork.run(em -> {
        GravBlog reload = PersistentWork.reload(item);
        em.remove(reload);
      });
    });
    store.reload();
  }
}
