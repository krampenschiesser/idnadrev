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
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.persistence.PersistentWork;
import de.ks.selection.CustomAutoCompletionBinding;
import de.ks.validation.validators.DoubleValidator;
import de.ks.validation.validators.NotEmptyValidator;
import de.ks.validation.validators.NotNullValidator;
import de.ks.validation.validators.TimeHHMMValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class AddBookingController extends BaseController<BookingViewModel> {
  public static final int MAX_AUTOCOMPLEITON_RESULTS = 2;//20;

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

  protected final SimpleStringProperty account = new SimpleStringProperty();
  private CustomAutoCompletionBinding categoryAutoCompletion;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    validationRegistry.registerValidator(time, new TimeHHMMValidator());
    validationRegistry.registerValidator(amount, new DoubleValidator());
    validationRegistry.registerValidator(amount, new NotEmptyValidator());
    validationRegistry.registerValidator(date, new NotNullValidator());

    book.disableProperty().bind(validationRegistry.invalidProperty());

    controller.getJavaFXExecutor().submit(() -> categoryAutoCompletion = new CustomAutoCompletionBinding(category, this::supplyCategoryCompletion));
  }

  protected List<String> supplyCategoryCompletion(AutoCompletionBinding.ISuggestionRequest request) {
    return PersistentWork.projection(Booking.class, true, MAX_AUTOCOMPLEITON_RESULTS, b -> b.getCategory(), (root, query, builder) -> {
      Path<String> categoryPath = root.get(BookingLoadingHint.KEY_CATEGORY);
      Expression<String> categoryPathLower = builder.lower(categoryPath);
      String pattern = request.getUserText().toLowerCase(Locale.ROOT).trim() + "%";
      query.where(builder.like(categoryPathLower, pattern));
      query.orderBy(builder.asc(categoryPath));
    });
  }

  @FXML
  protected void onBooking() {
    PersistentWork.run(em -> {
      Account acc = PersistentWork.forName(Account.class, account.getValue());
      Booking booking = new Booking(acc, Long.valueOf(amount.getText()));
      em.persist(booking);
    });
    controller.reload();
  }

  public String getAccount() {
    return account.get();
  }

  public SimpleStringProperty accountProperty() {
    return account;
  }

  public void setAccount(String account) {
    this.account.set(account);
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    date.setValue(LocalDate.now().minusMonths(3));
  }
}
