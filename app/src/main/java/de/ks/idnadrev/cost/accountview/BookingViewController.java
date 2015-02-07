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
package de.ks.idnadrev.cost.accountview;

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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BookingViewController extends BaseController<BookingViewModel> {

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
  protected TableView<Booking> bookingTable;
  @FXML
  protected TableColumn<Booking, String> timeColumn;
  @FXML
  protected TableColumn<Booking, String> descriptionColumn;
  @FXML
  protected TableColumn<Booking, String> categoryColumn;
  @FXML
  protected TableColumn<Booking, Double> amountColumn;
  @FXML
  protected TableColumn<Booking, Double> totalColumn;

  @FXML
  protected AddBookingController addBookingController;

  protected DateTimeFormatter dateTimeFormatter;

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
      Booking booking = e.getRowValue();
      PersistentWork.wrap(() -> {
        PersistentWork.reload(booking).setAmount(newValue);
      });
    });

    totalColumn.setCellValueFactory(c -> (ObservableValue) new SimpleDoubleProperty(c.getValue().getTotal()));


    validationRegistry.registerValidator(startTime, new NotNullValidator());
    validationRegistry.registerValidator(endTime, new NotNullValidator());
    validationRegistry.registerValidator(amount, new DoubleValidator());

    addBookingController.accountProperty().bind(account.valueProperty());

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
      hint.setAmount(Double.valueOf(amt));
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
    bookingTable.setItems(FXCollections.observableArrayList(model.getBookings()));

  }
}
