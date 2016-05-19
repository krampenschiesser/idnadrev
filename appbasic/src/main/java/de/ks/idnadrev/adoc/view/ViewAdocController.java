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
package de.ks.idnadrev.adoc.view;

import de.ks.idnadrev.adoc.AdocAccessor;
import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.adoc.ui.AdocPreview;
import de.ks.idnadrev.crud.CRUDController;
import de.ks.idnadrev.util.ButtonHelper;
import de.ks.standbein.BaseController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.reactfx.EventStreams;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewAdocController extends BaseController<List<AdocFile>> {
  @FXML
  private VBox root;
  @FXML
  private SplitPane split;

  @FXML
  private CRUDController crudController;
  @FXML
  private AdocFilter adocFilterController;
  @FXML
  private AdocTable adocTableController;
  @FXML
  private AdocPreview previewController;

  @Inject
  ButtonHelper buttonHelper;
  @Inject
  AdocAccessor adocAccessor;
  private Button edit;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ReadOnlyObjectProperty<AdocFile> selectedItemProperty = adocTableController.getAdocTable().getSelectionModel().selectedItemProperty();
    BooleanBinding itemIsNull = selectedItemProperty.isNull();
    crudController.getDeleteButton().disableProperty().bind(itemIsNull);

    edit = buttonHelper.createImageButton(localized.get("edit"), "edit.png", 24);
    edit.disableProperty().bind(itemIsNull);
    crudController.getCenterButtonContainer().getChildren().addAll(edit);

    previewController.selectedTaskProperty().bind(selectedItemProperty);

    edit.setOnAction(e -> edit(selectedItemProperty.get()));
    crudController.getDeleteButton().setOnAction(e -> delete(selectedItemProperty.get()));

    EventStreams.eventsOf(adocTableController.getAdocTable(), KeyEvent.KEY_RELEASED)//
      .filter(e -> e.getCode() == KeyCode.DELETE)//
      .subscribe(e -> delete(selectedItemProperty.get()));
  }

  private void delete(AdocFile task) {
    adocAccessor.delete(task);
    controller.reload();
  }

  private void edit(AdocFile task) {
//    ActivityHint hint = new ActivityHint(AddAdocFileActivity.class, controller.getCurrentActivityId()).setDataSourceHint(() -> task);
//    controller.startOrResume(hint);
  }
}
