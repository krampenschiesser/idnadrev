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
package de.ks.idnadrev.cost.csvimport;

import de.ks.BaseController;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.BookingCsvTemplate;
import de.ks.persistence.PersistentWork;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CSVParseDefinitionController extends BaseController<Object> {
  @FXML
  protected Button saveTemplate;
  @FXML
  protected TextField datePattern;
  @FXML
  protected ComboBox<BookingCsvTemplate> templates;
  @FXML
  protected ComboBox<String> account;
  @FXML
  protected TextField amountColumn;
  @FXML
  protected TextField dateColumn;
  @FXML
  protected TextField timeColumn;
  @FXML
  protected TextField timePattern;
  @FXML
  protected TextField separator;
  @FXML
  protected TextField descriptionColumn;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    templates.setConverter(new StringConverter<BookingCsvTemplate>() {
      @Override
      public String toString(BookingCsvTemplate object) {
        return object.getName();
      }

      @Override
      public BookingCsvTemplate fromString(String string) {
        return PersistentWork.forName(BookingCsvTemplate.class, string);
      }
    });

    templates.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null) {
        String accountName = n.getAccount().getName();
        account.getSelectionModel().select(accountName);
        separator.setText(n.getSeparator());
        timePattern.setText(n.getTimePattern());
        datePattern.setText(n.getDatePattern());

        dateColumn.setText(String.valueOf(n.getDateColumn()));
        timeColumn.setText(String.valueOf(n.getTimeColumn()));
        descriptionColumn.setText(String.valueOf(n.getDescriptionColumn()));
        amountColumn.setText(n.getAmountColumnString());
      }
    });
  }

  @FXML
  protected void onFillFromTemplate() {

  }

  @FXML
  protected void onSaveTemplate() {

  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    CompletableFuture.supplyAsync(() -> PersistentWork.from(BookingCsvTemplate.class), controller.getExecutorService())//
      .thenAcceptAsync(loaded -> {
        templates.getItems().clear();
        templates.getItems().addAll(loaded);
        if (!loaded.isEmpty()) {
          templates.getSelectionModel().select(0);
          BookingCsvTemplate selectedItem = templates.getSelectionModel().getSelectedItem();
          if (selectedItem.getAccount() != null) {
            account.getSelectionModel().select(selectedItem.getAccount().getName());
          }
        }
      }, controller.getJavaFXExecutor());

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
}
