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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChartDataEditor extends BaseController<ChartInfo> {
  private static final Logger log = LoggerFactory.getLogger(ChartDataEditor.class);
  private static final int ROW_OFFSET = 1;
  private static final int COLUMN_OFFSET = 1;
  @FXML
  public TextField xaxisTitle;
  @FXML
  protected GridPane dataContainer;
  @FXML
  protected GridPane root;

  protected final ObservableList<ChartRow> rows = FXCollections.observableArrayList();
  protected final ObservableList<SimpleStringProperty> columnHeaders = FXCollections.observableArrayList();

  protected final List<TextField> headers = new ArrayList<>();
  protected final List<TextField> categoryEditors = new ArrayList<>();
  protected final Table<Integer, Integer, TextField> valueEditors = HashBasedTable.create();

  protected Consumer<ChartData> callback;

  @Override
  public void initialize(URL location, ResourceBundle resources) {


    columnHeaders.addListener((ListChangeListener<SimpleStringProperty>) c -> onColumnsChanged(c));
    rows.addListener((ListChangeListener<ChartRow>) c -> onRowsChanged(c));

    reset();
    validationRegistry.registerValidator(xaxisTitle, new NotEmptyValidator());
  }

  protected void onRowsChanged(ListChangeListener.Change<? extends ChartRow> c) {
    while (c.next()) {
      List<? extends ChartRow> addedSubList = c.getAddedSubList();

      for (ChartRow chartRow : addedSubList) {
        int rowNum = rows.indexOf(chartRow);

        TextField categoryEditor = createCategoryEditor(chartRow, rowNum);
        addRowConstraint();
        dataContainer.add(categoryEditor, 0, rowNum + ROW_OFFSET);

        for (int i = 0; i < columnHeaders.size(); i++) {
          TextField editor = createValueEditor(chartRow, rowNum, i);
          SimpleStringProperty value = chartRow.getValue(i);
          editor.textProperty().bindBidirectional(value);
        }
      }
    }
  }

  private TextField createCategoryEditor(ChartRow chartRow, int rowNum) {
    TextField categoryEditor = new TextField();
    categoryEditor.textProperty().bindBidirectional(chartRow.getCategory());

    categoryEditor.focusedProperty().addListener(getEditorFocusListener(rowNum, categoryEditor));

    categoryEditor.textProperty().addListener((p, o, n) -> {
      categoryEditor.setUserData(true);
    });
    BiFunction<Integer, Integer, TextField> nextCategoryField = (row, column) -> {
      if (categoryEditors.size() > row) {
        return categoryEditors.get(row);
      } else {
        return null;
      }
    };
    BiConsumer<Integer, Integer> clipBoardHandler = (row, col) -> {
      String string = Clipboard.getSystemClipboard().getString();
      if (StringUtils.containsWhitespace(string)) {
        List<String> datas = Arrays.asList(StringUtils.split(string, "\n"));
        int missingRows = (row + datas.size()) - rows.size();
        if (missingRows > 0) {
          for (int i = 0; i < missingRows; i++) {
            rows.add(new ChartRow());
          }
        }
        for (int i = row; i < row + datas.size(); i++) {
          ChartRow currentChartRow = rows.get(i);
          String data = datas.get(i - row);
          currentChartRow.setCategory(data);
        }
      }
    };
    categoryEditor.setOnKeyReleased(getInputKeyHandler(rowNum, -1, nextCategoryField, clipBoardHandler));

    validationRegistry.registerValidator(categoryEditor, (control, value) -> {
      if (value != null) {
        Set<String> values = categoryEditors.stream()//
          .filter(e -> e != categoryEditor)//
          .map(e -> e.textProperty().getValueSafe())//
          .filter(v -> !v.isEmpty())//
          .collect(Collectors.toSet());
        if (values.contains(value)) {
          ValidationMessage message = new ValidationMessage("validation.noDuplicates", control, value);
          return ValidationResult.fromMessages(message);
        }
      }
      return null;
    });
    categoryEditors.add(categoryEditor);
    return categoryEditor;
  }

  protected void onColumnsChanged(ListChangeListener.Change<? extends SimpleStringProperty> c) {
    while (c.next()) {
      List<? extends SimpleStringProperty> added = c.getAddedSubList();
      List<? extends SimpleStringProperty> removed = c.getRemoved();

      for (SimpleStringProperty column : added) {
        int columnIndex = columnHeaders.indexOf(column);
        addColumnConstraint();

        TextField title = new TextField();
        title.textProperty().bindBidirectional(column);
        title.getStyleClass().add("editorViewLabel");

        MenuItem deleteColumnItem = new MenuItem(Localized.get("column.delete"));
        deleteColumnItem.setOnAction(e -> {
          columnHeaders.remove(column);
        });
        title.setContextMenu(new ContextMenu(deleteColumnItem));

        headers.add(title);
        dataContainer.add(title, columnIndex + COLUMN_OFFSET, 0);

        for (int i = 0; i < rows.size(); i++) {
          ChartRow chartRow = rows.get(i);
          SimpleStringProperty value = chartRow.getValue(columnIndex);

          TextField editor = createValueEditor(chartRow, i, columnIndex);
          editor.textProperty().bindBidirectional(value);
        }
      }
      for (SimpleStringProperty column : removed) {
        Optional<Integer> first = dataContainer.getChildren().stream().filter(n -> GridPane.getRowIndex(n) == 0).map(n -> (TextField) n).filter(t -> t.getText().equals(column.getValue())).map(t -> GridPane.getColumnIndex(t)).findFirst();
        if (first.isPresent()) {
          int columnIndex = first.get();
          rows.forEach(r -> {
            SimpleStringProperty value = r.getValue(columnIndex);
            value.set("");
            value.unbind();
          });
          List<Node> childrenToRemove = dataContainer.getChildren().stream().filter(n -> GridPane.getColumnIndex(n) == columnIndex).collect(Collectors.toList());
          dataContainer.getChildren().removeAll(childrenToRemove);
          dataContainer.getColumnConstraints().remove(dataContainer.getColumnConstraints().size() - 1);
        }
      }

      sortGridPane();
    }
  }

  protected void sortGridPane() {
    ArrayList<Node> childrensToSort = new ArrayList<>(dataContainer.getChildren());

    Comparator<Node> rowCompare = Comparator.comparing(GridPane::getRowIndex);
    Comparator<Node> columnCompare = Comparator.comparing(GridPane::getColumnIndex);

    Collections.sort(childrensToSort, rowCompare.thenComparing(columnCompare));

    dataContainer.getChildren().clear();
    dataContainer.getChildren().addAll(childrensToSort);
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
    dataContainer.add(editor, column + COLUMN_OFFSET, rowNum + ROW_OFFSET);

    editor.focusedProperty().addListener(getEditorFocusListener(rowNum, editor));

    editor.textProperty().addListener((p, o, n) -> {
      editor.setUserData(true);
    });

    BiFunction<Integer, Integer, TextField> nextTextField = (row, col) -> valueEditors.row(row).get(col);
    BiConsumer<Integer, Integer> clipBoardHandler = (row, col) -> {
      String string = Clipboard.getSystemClipboard().getString();
      if (StringUtils.containsWhitespace(string)) {
        List<String> datas = Arrays.asList(StringUtils.split(string));
        int missingRows = (row + datas.size()) - rows.size();
        if (missingRows > 0) {
          for (int i = 0; i < missingRows; i++) {
            rows.add(new ChartRow());
          }
        }
        for (int i = row; i < row + datas.size(); i++) {
          ChartRow currentChartRow = rows.get(i);
          String data = datas.get(i - row);
          currentChartRow.setValue(column, data);
        }
      }
    };
    editor.setOnKeyReleased(getInputKeyHandler(rowNum, column, nextTextField, clipBoardHandler));
    return editor;
  }

  private EventHandler<KeyEvent> getInputKeyHandler(int rowNum, int column, BiFunction<Integer, Integer, TextField> nextTextField, BiConsumer<Integer, Integer> clipBoardHandler) {
    return e -> {
      KeyCode code = e.getCode();
      if (e.isControlDown() && code == KeyCode.V) {
        clipBoardHandler.accept(rowNum, column);
        e.consume();
      }
      boolean selectNext = false;
      if (e.getCode() == KeyCode.ENTER && !e.isControlDown()) {
        selectNext = true;
      }
      if (selectNext) {
        int next = rowNum + 1;
        TextField textField = nextTextField.apply(next, column);
        if (textField != null) {
          textField.requestFocus();
        }
        e.consume();
      }
    };
  }

  private ChangeListener<Boolean> getEditorFocusListener(int rowNum, TextField editor) {
    return (p, o, n) -> {
      if (n) {
        if (!isRowEmpty(rowNum) && rowNum + ROW_OFFSET == rows.size()) {
          rows.add(new ChartRow());
        }
        editor.setUserData(false);
      } else if (o && !n) {
        boolean edited = (Boolean) (editor.getUserData() == null ? false : editor.getUserData());
        if (edited) {
          triggerRedraw();
          editor.setUserData(false);
        }
      }
    };
  }

  private boolean isRowEmpty(int rowNum) {
    ChartRow row = rows.get(rowNum);
    return row.getCategory().getValueSafe().trim().isEmpty();
  }

  protected void triggerRedraw() {
    if (callback != null && !validationRegistry.isInvalid()) {
      callback.accept(getData());
    }
  }

  public void addColumnHeader(String title) {
    columnHeaders.add(new SimpleStringProperty(title));
  }

  public ObservableList<ChartRow> getRows() {
    return rows;
  }

  public ObservableList<SimpleStringProperty> getColumnHeaders() {
    return columnHeaders;
  }

  public List<TextField> getHeaders() {
    return headers;
  }

  public List<TextField> getCategoryEditors() {
    return categoryEditors;
  }

  public ChartData getData() {
    ChartData data = new ChartData();
    this.rows.forEach(r -> {
      data.getCategories().add(r.getCategory().getValueSafe());
    });

    for (int i = 0; i < columnHeaders.size(); i++) {
      SimpleStringProperty header = columnHeaders.get(i);
      LinkedList<Double> values = new LinkedList<>();
      for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
        ChartRow row = rows.get(rowNum);
        if (row.getCategory().getValueSafe().trim().isEmpty()) {
          continue;
        }
        SimpleStringProperty value = row.getValue(i);
        if (value != null && !value.getValueSafe().trim().isEmpty()) {
          double val = Double.parseDouble(value.getValueSafe());
          values.add(val);
        } else {
          values.add(0d);
        }
      }
      data.addSeries(header.getValueSafe(), values);
    }
    data.setXAxisTitle(xaxisTitle.getText());
    return data;
  }

  public void setData(ChartData data) {
    int row = 0;
    for (String category : data.getCategories()) {
      if (rows.size() < row + 1) {
        rows.add(new ChartRow());
      }
      rows.get(row).setCategory(category);
      row++;
    }
    int column = 0;
    for (ChartData.DataSeries dataSeries : data.getSeries()) {
      if (columnHeaders.size() < column + 1) {
        columnHeaders.add(new SimpleStringProperty());
      }
      columnHeaders.get(column).set(dataSeries.getTitle());

      for (int valueIndex = 0; valueIndex < dataSeries.getValues().size(); valueIndex++) {
        Double value = dataSeries.getValues().get(valueIndex);
        rows.get(valueIndex).setValue(column, value);
      }
      column++;
    }
    xaxisTitle.setText(data.getXAxisTitle());
  }

  public void setCallback(Consumer<ChartData> callback) {
    this.callback = callback;
  }

  @Override
  public void duringSave(ChartInfo model) {
    model.setChartData(getData());
  }

  @Override
  public void duringLoad(ChartInfo model) {
    model.getChartData();//deserialize async
  }

  public void reset() {
    dataContainer.getChildren().clear();
    dataContainer.getChildren().remove(xaxisTitle);//FXML loader doesn't set row and column in gridpane :(
    dataContainer.add(xaxisTitle, 0, 0);

    rows.clear();
    columnHeaders.clear();
    categoryEditors.clear();
    valueEditors.clear();
    headers.clear();

    rows.add(new ChartRow());
    columnHeaders.add(new SimpleStringProperty(Localized.get("col", 1)));
    columnHeaders.add(new SimpleStringProperty(Localized.get("col", 2)));
    if (callback != null) {
      callback.accept(getData());
    }
  }
}
