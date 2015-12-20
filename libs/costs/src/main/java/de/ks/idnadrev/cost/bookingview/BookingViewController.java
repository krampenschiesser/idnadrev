/*
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

import de.ks.executor.group.LastTextChange;
import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.cost.entity.Account;
import de.ks.idnadrev.cost.entity.Booking;
import de.ks.idnadrev.cost.pattern.view.BookingPatternParser;
import de.ks.standbein.BaseController;
import de.ks.standbein.javafx.event.ClearTextOnEscape;
import de.ks.standbein.validation.validators.DoubleValidator;
import de.ks.standbein.validation.validators.NotNullValidator;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
  protected Button applyPatterns;

  @FXML
  protected BookingViewTableController bookingTableController;

  @Inject
  BookingPatternParser patternParser;
  @Inject
  PersistentWork persistentWork;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    startTime.setValue(LocalDate.now().minusMonths(3));
    endTime.setValue(LocalDate.now().plusDays(1));

    validationRegistry.registerValidator(startTime, new NotNullValidator(localized));
    validationRegistry.registerValidator(endTime, new NotNullValidator(localized));
    validationRegistry.registerValidator(amount, new DoubleValidator(localized));

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
    CompletableFuture.supplyAsync(() -> persistentWork.from(Account.class), controller.getExecutorService())//
      .thenAcceptAsync(accounts -> {
        List<String> accountNames = accounts.stream().map(a -> a.getName()).collect(Collectors.toList());
        account.getItems().clear();
        account.getItems().addAll(accountNames);
        if (!accountNames.isEmpty()) {
          account.getSelectionModel().select(0);
        }
      }, controller.getJavaFXExecutor());
  }

  @FXML
  public void onDelete() {
    boolean interactive = false;
    onDelete(interactive);
  }

  protected void onDelete(boolean interactive) {
    List<Booking> bookingsToDelete = bookingTableController.getMarked().entrySet().stream().filter(e -> e.getValue().get()).map(e -> e.getKey()).collect(Collectors.toList());

    Optional optional;
    if (interactive) {
      Dialog dialog = new Dialog();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.setContentText(localized.get("bookings.delete", bookingsToDelete.size()));
      dialog.setTitle(localized.get("delete"));
      dialog.setOnCloseRequest(r -> dialog.close());
      ButtonType yes = new ButtonType(localized.get("yes"), ButtonBar.ButtonData.YES);
      ButtonType no = new ButtonType(localized.get("no"), ButtonBar.ButtonData.NO);
      dialog.getDialogPane().getButtonTypes().addAll(yes, no);

      optional = dialog.showAndWait().filter(response -> {
        ButtonType type = (ButtonType) response;
        return type.getButtonData() == ButtonBar.ButtonData.YES;
      });
    } else {
      optional = Optional.of(true);
    }
    optional.ifPresent(o -> {
      persistentWork.run(session -> {
        for (Booking booking : bookingsToDelete) {
          Booking reload = persistentWork.reload(booking);
          session.remove(reload);
        }
      });
      controller.reload();
    });
  }

  @FXML
  public void onApplyPatterns() {
    ObservableList<Booking> items = bookingTableController.getBookingTable().getItems();
    store.executeCustomRunnable(() -> {
      items.forEach(i -> {
        persistentWork.run(session -> {
          Booking reload = persistentWork.reload(i);
          String result = patternParser.parseLine(reload.getDescription());
          if (result != null) {
            reload.setCategory(result);
          }
        });
      });
    });
    store.reload();
  }

  protected TableView<Booking> getBookingTable() {
    return bookingTableController.bookingTable;
  }
}
