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
package de.ks.idnadrev.cost.pattern.view;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BookingPatternController extends BaseController<List<BookingPattern>> {

  @FXML
  protected TableView<BookingPattern> table;

  @FXML
  protected TableColumn<BookingPattern, String> nameColumn;
  @FXML
  protected TableColumn<BookingPattern, String> regexColumn;
  @FXML
  protected TableColumn<BookingPattern, String> categoryColumn;
  @FXML
  protected TableColumn<BookingPattern, Boolean> containsColumn;

  @FXML
  protected Button create;
  @FXML
  protected Button edit;
  @FXML
  protected Button delete;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ReadOnlyBooleanProperty invalidProperty = validationRegistry.invalidProperty();
    BooleanBinding nothingSelected = table.getSelectionModel().selectedItemProperty().isNull();

    edit.disableProperty().bind(nothingSelected.or(invalidProperty));
    delete.disableProperty().bind(nothingSelected.or(invalidProperty));
    create.disableProperty().bind(invalidProperty);

    table.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        onEdit();
        e.consume();
      }
      if (e.getCode() == KeyCode.DELETE) {
        onDelete();
        e.consume();
      }
    });
    nameColumn.setCellValueFactory(c -> (ObservableValue) new SimpleStringProperty(c.getValue().getName()));
    regexColumn.setCellValueFactory(c -> (ObservableValue) new SimpleStringProperty(c.getValue().getRegex()));
    categoryColumn.setCellValueFactory(c -> (ObservableValue) new SimpleStringProperty(c.getValue().getCategory()));
    containsColumn.setCellValueFactory(c -> (ObservableValue) new SimpleBooleanProperty(c.getValue().isSimpleContains()));
  }

  @Override
  protected void onRefresh(List<BookingPattern> model) {
    table.getItems().clear();
    table.getItems().addAll(model);
  }

  @FXML
  void onCreate() {
    ActivityHint hint = new ActivityHint(CreateEditPatternActivity.class, controller.getCurrentActivityId());
    controller.startOrResume(hint);
  }

  @FXML
  void onEdit() {
    BookingPattern item = table.getSelectionModel().getSelectedItem();

    ActivityHint hint = new ActivityHint(CreateEditPatternActivity.class, controller.getCurrentActivityId());
    hint.setDataSourceHint(() -> item);

    controller.startOrResume(hint);
  }

  @FXML
  void onDelete() {
    BookingPattern item = table.getSelectionModel().getSelectedItem();
    store.executeCustomRunnable(() -> {
      PersistentWork.run(em -> {
        BookingPattern reload = PersistentWork.reload(item);
        em.remove(reload);
      });
    });
    store.reload();
  }
}
