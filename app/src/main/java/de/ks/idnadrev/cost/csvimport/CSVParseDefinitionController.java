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
import de.ks.i18n.Localized;
import de.ks.idnadrev.cost.csvimport.columnmapping.*;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.BookingCsvTemplate;
import de.ks.persistence.PersistentWork;
import de.ks.validation.validators.DateTimeFormatterPatternValidator;
import de.ks.validation.validators.IntegerRangeValidator;
import de.ks.validation.validators.NotEmptyValidator;
import de.ks.validation.validators.StringLengthValidator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
  protected CheckBox useComma;
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
        if (n.getTimeColumn() != null) {
          timeColumn.setText(String.valueOf(n.getTimeColumn()));
        }
        descriptionColumn.setText(String.valueOf(n.getDescriptionColumn()));
        amountColumn.setText(n.getAmountColumnString());
      }
    });

    validationRegistry.registerValidator(timePattern, new DateTimeFormatterPatternValidator());
    validationRegistry.registerValidator(datePattern, new DateTimeFormatterPatternValidator());

    validationRegistry.registerValidator(descriptionColumn, new IntegerRangeValidator(0, 100));
    validationRegistry.registerValidator(timeColumn, new IntegerRangeValidator(0, 100));
    validationRegistry.registerValidator(dateColumn, new IntegerRangeValidator(0, 100));
    validationRegistry.registerValidator(amountColumn, new AmountColumnValidator());

    validationRegistry.registerValidator(separator, new StringLengthValidator(1));

    Arrays.asList(datePattern, dateColumn, descriptionColumn, amountColumn)//
      .forEach(t -> validationRegistry.registerValidator(t, new NotEmptyValidator()));

    saveTemplate.disableProperty().bind(validationRegistry.invalidProperty());
  }

  public BookingFromCSVImporter getImporter() {
    return new BookingFromCSVImporter(separator.getText(), getColumnMappings());
  }

  protected List<BookingColumnMapping<?>> getColumnMappings() {
    ArrayList<BookingColumnMapping<?>> retval = new ArrayList<>();
    List<AmountColumnMapping> amountColumns = Arrays.asList(amountColumn.getText().split("\\,")).stream().mapToInt(Integer::valueOf).mapToObj(i -> new AmountColumnMapping(i, useComma.isSelected())).collect(Collectors.toList());
    retval.addAll(amountColumns);

    retval.add(new DescriptionColumnMapping(Integer.valueOf(descriptionColumn.getText())));
    if (!timeColumn.textProperty().getValueSafe().trim().isEmpty()) {
      Integer column = Integer.valueOf(timeColumn.getText());
      String pattern = timePattern.getText();
      retval.add(new BookingTimeColumnMapping(column, pattern));
    }
    retval.add(new BookingDateColumnMapping(Integer.valueOf(dateColumn.getText()), datePattern.getText()));

    return retval;
  }

  @FXML
  protected void onSaveTemplate() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setContentText(Localized.get("import.template.name"));
    dialog.setTitle(Localized.get("enter.input"));
    dialog.setOnCloseRequest(r -> dialog.close());

    dialog.showAndWait().ifPresent(s -> onSaveTemplate(s));
  }

  protected void onSaveTemplate(String templateName) {
    BookingCsvTemplate readTemplate = PersistentWork.read(em -> {
      BookingCsvTemplate template = PersistentWork.forName(BookingCsvTemplate.class, templateName);
      template = template != null ? template : new BookingCsvTemplate(templateName);
      if (timeColumn.textProperty().getValueSafe().trim().isEmpty()) {
        template.setTimeColumn(-1);
      } else {
        template.setTimeColumn(Integer.valueOf(timeColumn.getText()));
      }
      template.setDescriptionColumn(Integer.valueOf(descriptionColumn.getText()));
      template.setDateColumn(Integer.valueOf(dateColumn.getText()));
      template.setAmountColumnString(amountColumn.getText());
      template.setDatePattern(datePattern.getText());
      template.setTimePattern(timePattern.textProperty().getValueSafe());
      template.setSeparator(separator.getText());
      Account account = PersistentWork.forName(Account.class, this.account.getValue());
      template.setAccount(account);
      PersistentWork.persist(template);
      return template;
    });
    templates.getItems().remove(readTemplate);
    templates.getItems().add(readTemplate);
    templates.getSelectionModel().select(readTemplate);
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
