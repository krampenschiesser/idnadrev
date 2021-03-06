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
package de.ks.idnadrev.information.diary;

import de.ks.BaseController;
import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.information.DiaryInfo;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.text.AsciiDocEditor;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;

public class DiaryController extends BaseController<DiaryInfo> {

  @FXML
  protected DatePicker dateEditor;
  @FXML
  protected StackPane adocContainer;

  protected AsciiDocEditor content;
  protected String baseText;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(pane -> adocContainer.getChildren().add(pane), editor -> {
      this.content = editor;
      editor.hideActionBar();
    });

    StringProperty contentProperty = store.getBinding().getStringProperty(TextInfo.class, t -> t.getContent());
    content.textProperty().bindBidirectional(contentProperty);
    dateEditor.setValue(LocalDate.now());

    dateEditor.setDayCellFactory(picker -> {
      return new DateCell() {
        @Override
        public void updateItem(LocalDate item, boolean empty) {
          super.updateItem(item, empty);

          DiaryDS datasource = (DiaryDS) store.getDatasource();
          if (datasource.getDates().contains(item)) {
            getStyleClass().add("activeDateCell");
          } else {
            getStyleClass().remove("activeDateCell");
          }
        }
      };
    });

    dateEditor.valueProperty().addListener((p, o, n) -> {
      if (n != null) {
        DataSource<?> datasource = store.getDatasource();
        datasource.setLoadingHint(n);
        controller.save();
        controller.reload();
      }
    });
  }

  @Override
  protected void onRefresh(DiaryInfo model) {
    if (model.getId() == 0) {
      String date = dateEditor.getValue().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
      baseText = "= " + date + "\n\n";
      content.setText(baseText);
      content.selectEditor();
    } else {
      content.selectPreview();
    }
  }

  @Override
  public void duringSave(DiaryInfo model) {
    super.duringSave(model);
    if (model.getContent().equals(baseText)) {
      model.setContent("");
    }
  }

  @Override
  public void onSuspend() {
    controller.save();
  }

  @Override
  public void onStop() {
    controller.save();
  }

  @FXML
  void onPrevious() {
    dateEditor.setValue(dateEditor.getValue().minusDays(1));
  }

  @FXML
  void onNext() {
    dateEditor.setValue(dateEditor.getValue().plusDays(1));
  }
}
