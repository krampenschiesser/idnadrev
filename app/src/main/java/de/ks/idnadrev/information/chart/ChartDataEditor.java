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
package de.ks.idnadrev.information.chart;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.ks.BaseController;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.validation.validators.DoubleValidator;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ChartDataEditor extends BaseController<ChartInfo> {
  private static final Logger log = LoggerFactory.getLogger(ChartDataEditor.class);
  @FXML
  protected Button addColumn;
  @FXML
  protected GridPane dataContainer;
  @FXML
  protected GridPane root;

  protected final ObservableList<ChartRow> rows = FXCollections.observableArrayList();
  protected final ObservableList<String> columnHeaders = FXCollections.observableArrayList();

  protected final List<TextField> headers = new ArrayList<>();
  protected final List<TextField> categoryEditors = new ArrayList<>();
  protected final Table<Integer, Integer, TextField> valueEditors = HashBasedTable.create();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    columnHeaders.addListener((ListChangeListener<String>) c -> onColumnsChanged(c));
    rows.addListener((ListChangeListener<ChartRow>) c -> onRowsChanged(c));
    rows.add(new ChartRow());
    columnHeaders.add("col1");
    columnHeaders.add("col2");
  }

  protected void onRowsChanged(ListChangeListener.Change<? extends ChartRow> c) {
    while (c.next()) {
      List<? extends ChartRow> addedSubList = c.getAddedSubList();

      for (ChartRow chartRow : addedSubList) {
        int rowNum = rows.indexOf(chartRow);

        TextField categoryEditor = createCategoryEditor(chartRow, rowNum);
        addRowConstraint();
        dataContainer.add(categoryEditor, 0, rowNum + 1);

        for (int i = 0; i < columnHeaders.size(); i++) {
          TextField editor = createValueEditor(chartRow, rowNum, i);
          editor.setText(chartRow.getValue(i));
        }
      }
    }
  }

  private TextField createCategoryEditor(ChartRow chartRow, int rowNum) {
    TextField categoryEditor = new TextField();
    categoryEditor.setText(chartRow.getCategory());
    categoryEditor.focusedProperty().addListener((p, o, n) -> {
      if (n) {
        if (rowNum + 1 == rows.size()) {
          rows.add(new ChartRow());
        }
      }
    });
    categoryEditor.setOnKeyTyped(e -> {
      boolean selectNext = false;
      if (e.getCode() == KeyCode.UNDEFINED) {
        if (e.getCharacter().equals("\r")) {
          selectNext = true;
        }
      } else if (e.getCode() == KeyCode.ENTER) {
        selectNext = true;
      }
      if (selectNext) {
        int next = rowNum + 1;
        if (categoryEditors.size() > next) {
          categoryEditors.get(next).requestFocus();
        }
        e.consume();
      }
    });
    categoryEditors.add(categoryEditor);
    return categoryEditor;
  }

  protected void onColumnsChanged(ListChangeListener.Change<? extends String> c) {
    while (c.next()) {
      List<? extends String> added = c.getAddedSubList();

      for (String column : added) {
        int columnIndex = columnHeaders.indexOf(column);
        addColumnConstraint();

        TextField title = new TextField(column);
        title.getStyleClass().add("title");
        headers.add(title);
        dataContainer.add(title, columnIndex + 1, 0);

        for (int i = 0; i < rows.size(); i++) {
          ChartRow chartRow = rows.get(i);
          String value = chartRow.getValue(columnIndex);

          TextField editor = createValueEditor(chartRow, i, columnIndex);
          editor.setText(value);
        }
      }
    }
  }

  protected void addRowConstraint() {
    dataContainer.getRowConstraints().add(new RowConstraints(30, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.NEVER, VPos.CENTER, true));
  }

  protected void addColumnConstraint() {
    dataContainer.getColumnConstraints().add(new ColumnConstraints(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true));
  }

  protected TextField createValueEditor(ChartRow chartRow, int rowNum, int column) {
    TextField editor = new TextField();
    valueEditors.put(rowNum, column, editor);
    validationRegistry.registerValidator(editor, new DoubleValidator());
    dataContainer.add(editor, column + 1, rowNum + 1);

    editor.focusedProperty().addListener((p, o, n) -> {
      if (n) {
        if (rowNum + 1 == rows.size()) {
          rows.add(new ChartRow());
        }
      }
    });
    editor.textProperty().addListener((p, o, n) -> {
      chartRow.setValue(column, n);
    });
    editor.setOnKeyTyped(e -> {
      boolean selectNext = false;
      if (e.getCode() == KeyCode.UNDEFINED) {
        if (e.getCharacter().equals("\r")) {
          selectNext = true;
        }
      } else if (e.getCode() == KeyCode.ENTER) {
        selectNext = true;
      }
      if (selectNext) {
        int next = rowNum + 1;
        if (valueEditors.containsRow(next)) {
          TextField textField = valueEditors.row(next).get(column);
          textField.requestFocus();
        }
        e.consume();
      }
    });
    return editor;
  }

  @FXML
  void onAddColumn() {
    Optional<String> input = Dialogs.create().message(Localized.get("column.title")).showTextInput();
    if (input.isPresent()) {
      addColumnHeader(input.get());
    }
  }

  public void addColumnHeader(String title) {
    columnHeaders.add(title);
  }

  public ObservableList<ChartRow> getRows() {
    return rows;
  }

  public ObservableList<String> getColumnHeaders() {
    return columnHeaders;
  }

  public List<TextField> getHeaders() {
    return headers;
  }

  public List<TextField> getCategoryEditors() {
    return categoryEditors;
  }
}
