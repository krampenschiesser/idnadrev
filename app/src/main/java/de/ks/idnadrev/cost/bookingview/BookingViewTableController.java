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
package de.ks.idnadrev.cost.bookingview;

import de.ks.BaseController;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.persistence.PersistentWork;
import de.ks.validation.cell.ValidatingTableCell;
import de.ks.validation.validators.DoubleValidator;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.util.converter.DoubleStringConverter;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BookingViewTableController extends BaseController<BookingViewModel> {
  @FXML
  protected TableView<Booking> bookingTable;
  @FXML
  protected TableColumn<Booking, Boolean> markedColumn;
  @FXML
  protected TableColumn<Booking, String> timeColumn;
  @FXML
  protected TableColumn<Booking, String> descriptionColumn;
  @FXML
  protected TableColumn<Booking, String> categoryColumn;
  @FXML
  protected TableColumn<Booking, Double> amountColumn;

  protected DateTimeFormatter dateTimeFormatter;
  protected final Map<Booking, SimpleBooleanProperty> marked = new HashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    String pattern = Localized.get("fullDate");
    dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    bookingTable.setEditable(true);

    timeColumn.setCellValueFactory(c -> new SimpleStringProperty(dateTimeFormatter.format(c.getValue().getBookingTime())));

    descriptionColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
    descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    descriptionColumn.setOnEditCommit(e -> {
      String newCat = e.getNewValue();
      Booking booking = e.getRowValue();
      PersistentWork.wrap(() -> {
        PersistentWork.reload(booking).setDescription(newCat);
      });
    });
    categoryColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
    categoryColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    categoryColumn.setOnEditCommit(e -> {
      String newCat = e.getNewValue();
      Booking booking = e.getRowValue();
      PersistentWork.wrap(() -> {
        PersistentWork.reload(booking).setCategory(newCat);
      });
    });

    amountColumn.setCellValueFactory(c -> (ObservableValue) new SimpleDoubleProperty(c.getValue().getAmount()));
    amountColumn.setCellFactory(b -> new ValidatingTableCell<Booking, Double>(new DoubleStringConverter(), new DoubleValidator()) {
      @Override
      public void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().clear();
        if (item != null && item.doubleValue() < 0) {
          getStyleClass().add("bookingNegative");
        } else if (item != null && item.doubleValue() > 0) {
          getStyleClass().add("bookingPositive");
        }
      }
    });
    amountColumn.setOnEditCommit(e -> {
      Double newValue = e.getNewValue();
      Double oldValue = e.getOldValue();
      Booking booking = e.getRowValue();
      if (!newValue.equals(oldValue)) {
        PersistentWork.wrap(() -> {
          PersistentWork.reload(booking).setAmount(newValue);
        });
        controller.reload();
      }
    });

    markedColumn.setCellFactory(column -> {
      if (column == null) {
        return null;
      } else {
        CheckBoxTableCell cell = new CheckBoxTableCell();
        cell.setSelectedStateCallback(i -> {
          Booking booking = bookingTable.getItems().get((int) i);
          SimpleBooleanProperty property = marked.get(booking);
          return property;
        });
        MenuItem selectItem = new MenuItem(Localized.get("select.all"));
        selectItem.setOnAction(e -> {
          marked.values().forEach(property -> property.set(true));
        });
        MenuItem deselectItem = new MenuItem(Localized.get("deselect.all"));
        deselectItem.setOnAction(e -> {
          marked.values().forEach(property -> property.set(false));
        });
        ContextMenu contextMenu = new ContextMenu(selectItem, deselectItem);
        cell.setContextMenu(contextMenu);
        return cell;
      }
    });

    bookingTable.setOnKeyPressed(e -> {
      TablePosition focusedCell = bookingTable.getFocusModel().getFocusedCell();
      int beginOfEditableColumns = 2;
      if (focusedCell.getColumn() < beginOfEditableColumns && !e.isControlDown() && !e.isAltDown()) {
        Booking item = bookingTable.getSelectionModel().getSelectedItem();
        if (e.getCode() == KeyCode.SPACE) {
          if (item != null) {
            SimpleBooleanProperty property = marked.get(item);
            property.set(!property.get());
            e.consume();
          }
        }
        if (e.getCode() == KeyCode.D) {
          e.consume();
          bookingTable.edit(bookingTable.getSelectionModel().getSelectedIndex(), descriptionColumn);
        }
        if (e.getCode() == KeyCode.C) {
          e.consume();
          bookingTable.edit(bookingTable.getSelectionModel().getSelectedIndex(), categoryColumn);
        }
      }
    });
  }

  @Override
  protected void onRefresh(BookingViewModel model) {
    ObservableList<Booking> bookings = FXCollections.observableArrayList(model.getBookings());
    bookingTable.setItems(bookings);

    marked.clear();
    for (Booking booking : bookings) {
      marked.put(booking, new SimpleBooleanProperty(false));
    }
  }

  public Map<Booking, SimpleBooleanProperty> getMarked() {
    return marked;
  }

  public TableColumn<Booking, Boolean> getMarkedColumn() {
    return markedColumn;
  }

}
