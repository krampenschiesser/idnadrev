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
package de.ks.idnadrev.cost.booking;

import de.ks.BaseController;
import de.ks.idnadrev.cost.bookingview.BookingLoadingHint;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.option.Options;
import de.ks.persistence.PersistentWork;
import de.ks.selection.CustomAutoCompletionBinding;
import de.ks.validation.validators.DoubleValidator;
import de.ks.validation.validators.NotEmptyValidator;
import de.ks.validation.validators.NotNullValidator;
import de.ks.validation.validators.TimeHHMMValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CreateBookingController extends BaseController<Booking> {
  public static final int MAX_AUTOCOMPLEITON_RESULTS = 2;//20;
  @FXML
  protected ComboBox<String> account;
  @FXML
  protected TextField category;
  @FXML
  protected TextField time;
  @FXML
  protected DatePicker date;
  @FXML
  protected TextField description;
  @FXML
  protected TextField amount;
  @FXML
  protected Button book;

  private CustomAutoCompletionBinding categoryAutoCompletion;
  private TimeHHMMValidator timeValidator;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    description.textProperty().bindBidirectional(store.getBinding().getStringProperty(Booking.class, b -> b.getDescription()));
    category.textProperty().bindBidirectional(store.getBinding().getStringProperty(Booking.class, b -> b.getCategory()));

    timeValidator = new TimeHHMMValidator();
    validationRegistry.registerValidator(time, timeValidator);
    validationRegistry.registerValidator(amount, new DoubleValidator());
    validationRegistry.registerValidator(amount, new NotEmptyValidator());
    validationRegistry.registerValidator(date, new NotNullValidator());

    book.disableProperty().bind(validationRegistry.invalidProperty());

    controller.getJavaFXExecutor().submit(() -> categoryAutoCompletion = new CustomAutoCompletionBinding(category, this::supplyCategoryCompletion));
  }

  protected List<String> supplyCategoryCompletion(AutoCompletionBinding.ISuggestionRequest request) {
    if (request.getUserText() == null || request.getUserText().isEmpty()) {
      return Collections.emptyList();
    } else {
      return PersistentWork.projection(Booking.class, true, MAX_AUTOCOMPLEITON_RESULTS, b -> b.getCategory(), (root, query, builder) -> {
        Path<String> categoryPath = root.get(BookingLoadingHint.KEY_CATEGORY);
        Expression<String> categoryPathLower = builder.lower(categoryPath);
        String pattern = request.getUserText().toLowerCase(Locale.ROOT).trim() + "%";
        query.where(builder.like(categoryPathLower, pattern));
        query.orderBy(builder.asc(categoryPath));
      });
    }
  }

  @FXML
  protected void onBooking() {
    controller.save();
    controller.reload();
  }

  @Override
  public void onStop() {
    onSuspend();
  }

  @Override
  public void onSuspend() {
    Options.store(account.getValue(), CreateBookingOptions.class).getDefaultAccount();
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    date.setValue(LocalDate.now());

    CreateBookingOptions options = Options.get(CreateBookingOptions.class);
    String defaultAccount = options.getDefaultAccount();

    CompletableFuture.supplyAsync(() -> PersistentWork.from(Account.class), controller.getExecutorService())//
            .thenAcceptAsync(accounts -> {
              List<String> accountNames = accounts.stream().map(a -> a.getName()).collect(Collectors.toList());
              account.getItems().clear();
              account.getItems().addAll(accountNames);
              if (!accountNames.isEmpty()) {
                if (accountNames.contains(defaultAccount)) {
                  account.getSelectionModel().select(defaultAccount);
                } else {
                  account.getSelectionModel().select(0);
                }
              }
            }, controller.getJavaFXExecutor());
  }

  @Override
  protected void onRefresh(Booking model) {
    amount.setText("");
    amount.requestFocus();
  }

  @Override
  public void duringSave(Booking model) {
    model.setAccount(PersistentWork.forName(Account.class, account.getValue()));
    model.setAmount(Double.parseDouble(amount.getText()));
    LocalTime bookingTime = timeValidator.getTime();
    if (bookingTime == null) {
      bookingTime = LocalTime.now();
    }
    LocalDate bookingDate = date.getValue();
    model.setBookingTime(LocalDateTime.of(bookingDate, bookingTime));
  }
}
