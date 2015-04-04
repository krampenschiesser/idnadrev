/**
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.idnadrev.cost.bookingview;

import de.ks.BaseController;
import de.ks.executor.group.LastTextChange;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.javafx.event.ClearTextOnEscape;
import de.ks.persistence.PersistentWork;
import de.ks.validation.cell.ValidatingTableCell;
import de.ks.validation.validators.DoubleValidator;
import de.ks.validation.validators.NotNullValidator;
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
import javafx.stage.Modality;
import javafx.util.converter.DoubleStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BookingViewController extends BaseController<BookingViewModel> {
  private static final Logger log = LoggerFactory.getLogger(BookingViewController.class);
  @FXML
  protected DatePicker startTime;
  @FXML
  protected DatePicker endTime;
  @FXML
  protected ComboBox<String> account;
  @FXML
  protected TextField amount;
  @FXML
  protected TextField description;
  @FXML
  protected TextField category;
  @FXML
  protected Button delete;

  @FXML
  protected TableView<Booking> bookingTable;
  @FXML
  protected TableColumn<Booking, Boolean> deleteColumn;
  @FXML
  protected TableColumn<Booking, String> timeColumn;
  @FXML
  protected TableColumn<Booking, String> descriptionColumn;
  @FXML
  protected TableColumn<Booking, String> categoryColumn;
  @FXML
  protected TableColumn<Booking, Double> amountColumn;

  protected DateTimeFormatter dateTimeFormatter;
  protected final Map<Booking, SimpleBooleanProperty> markedForDelete = new HashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    String pattern = Localized.get("fullDate");
    dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
    startTime.setValue(LocalDate.now().minusMonths(3));
    endTime.setValue(LocalDate.now().plusDays(1));
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
    amountColumn.setCellFactory(b -> new ValidatingTableCell<Booking, Double>(new DoubleStringConverter(), new DoubleValidator()));
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

    deleteColumn.setCellFactory(column -> {
      if (column == null) {
        return null;
      } else {
        CheckBoxTableCell cell = new CheckBoxTableCell();
        cell.setSelectedStateCallback(i -> {
          Booking booking = bookingTable.getItems().get((int) i);
          SimpleBooleanProperty property = markedForDelete.get(booking);
          return property;
        });
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
            SimpleBooleanProperty property = markedForDelete.get(item);
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

    validationRegistry.registerValidator(startTime, new NotNullValidator());
    validationRegistry.registerValidator(endTime, new NotNullValidator());
    validationRegistry.registerValidator(amount, new DoubleValidator());

    account.valueProperty().addListener((p, o, n) -> applyLoadingHintAndReload());
    startTime.valueProperty().addListener((p, o, n) -> applyLoadingHintAndReload());
    endTime.valueProperty().addListener((p, o, n) -> applyLoadingHintAndReload());

    new LastTextChange(amount, 500, controller.getExecutorService()).registerHandler(t -> applyLoadingHintAndReload());
    new LastTextChange(description, 500, controller.getExecutorService()).registerHandler(t -> applyLoadingHintAndReload());
    new LastTextChange(category, 500, controller.getExecutorService()).registerHandler(t -> applyLoadingHintAndReload());

    amount.setOnKeyReleased(new ClearTextOnEscape());
    description.setOnKeyReleased(new ClearTextOnEscape());
    category.setOnKeyReleased(new ClearTextOnEscape());
  }

  public void applyLoadingHintAndReload() {
    applyLoadingHint();
    store.reload();
  }

  protected void applyLoadingHint() {
    BookingLoadingHint hint = new BookingLoadingHint(account.getValue());
    hint.setStartDate(startTime.getValue());
    hint.setEndDate(endTime.getValue());

    String desc = description.textProperty().getValueSafe().trim();
    if (!desc.isEmpty()) {
      hint.setDescription(desc);
    }
    String amt = amount.textProperty().getValueSafe().trim();
    if (!amt.isEmpty()) {
      try {
        hint.setAmount(Double.valueOf(amt));
      } catch (NumberFormatException e) {
        log.trace("Could not set new value", amt);
      }
    }
    String cat = category.textProperty().getValueSafe().trim();
    if (!cat.isEmpty()) {
      hint.setCategory(cat);
    }

    store.getDatasource().setLoadingHint(hint);
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    startTime.setValue(LocalDate.now().minusMonths(3));
    endTime.setValue(LocalDate.now().plusDays(1));
    CompletableFuture.supplyAsync(() -> PersistentWork.from(Account.class), controller.getExecutorService())//
      .thenAcceptAsync(accounts -> {
        List<String> accountNames = accounts.stream().map(a -> a.getName()).collect(Collectors.toList());
        account.getItems().clear();
        account.getItems().addAll(accountNames);
        if (!accountNames.isEmpty()) {
          account.getSelectionModel().select(0);
        }
      }, controller.getJavaFXExecutor());
  }

  @Override
  protected void onRefresh(BookingViewModel model) {
    ObservableList<Booking> bookings = FXCollections.observableArrayList(model.getBookings());
    bookingTable.setItems(bookings);

    markedForDelete.clear();
    for (Booking booking : bookings) {
      markedForDelete.put(booking, new SimpleBooleanProperty(false));
    }
  }

  @FXML
  public void onDelete() {
    List<Booking> bookingsToDelete = markedForDelete.entrySet().stream().filter(e -> e.getValue().get()).map(e -> e.getKey()).collect(Collectors.toList());

    Dialog dialog = new Dialog();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setContentText(Localized.get("bookings.delete", bookingsToDelete.size()));
    dialog.setTitle(Localized.get("delete"));
    dialog.setOnCloseRequest(r -> dialog.close());
    ButtonType yes = new ButtonType(Localized.get("yes"), ButtonBar.ButtonData.YES);
    ButtonType no = new ButtonType(Localized.get("no"), ButtonBar.ButtonData.NO);
    dialog.getDialogPane().getButtonTypes().addAll(yes, no);

    dialog.showAndWait().filter(response -> {
      ButtonType type = (ButtonType) response;
      return type.getButtonData() == ButtonBar.ButtonData.YES;
    }).ifPresent(o -> {
      PersistentWork.run(em -> {
        for (Booking booking : bookingsToDelete) {
          Booking reload = PersistentWork.reload(booking);
          em.remove(reload);
        }
      });
      controller.reload();
    });
  }
}
