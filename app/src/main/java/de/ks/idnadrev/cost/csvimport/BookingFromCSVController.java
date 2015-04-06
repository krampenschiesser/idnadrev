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

import com.google.common.base.StandardSystemProperty;
import de.ks.BaseController;
import de.ks.i18n.Localized;
import de.ks.idnadrev.cost.bookingview.BookingViewTableController;
import de.ks.idnadrev.entity.cost.Booking;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BookingFromCSVController extends BaseController<ImporterBookingViewModel> {

  @FXML
  protected Accordion accordion;
  @FXML
  protected TitledPane lineTab;
  @FXML
  protected TitledPane errorTab;
  @FXML
  protected TextArea errorField;
  @FXML
  protected Button importer;
  @FXML
  protected TextField filePath;
  @FXML
  protected Button selectFile;

  @FXML
  protected CSVParseDefinitionController parseDefinitionController;

  @FXML
  protected BookingViewTableController bookingTableController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    controller.getJavaFXExecutor().submit(() -> accordion.setExpandedPane(lineTab));
    TableColumn<Booking, Boolean> markedColumn = bookingTableController.getMarkedColumn();
    markedColumn.setText(Localized.get("import"));

  }

  @Override
  protected void onRefresh(ImporterBookingViewModel model) {
    errorField.setText(model.getErrors());
  }

  @Override
  public void duringSave(ImporterBookingViewModel model) {
    List<Booking> bookingsToImport = bookingTableController.getMarked().entrySet().stream().filter(b -> b.getValue().get()).map(e -> e.getKey()).collect(Collectors.toList());
    model.setBookingsToImport(bookingsToImport);
  }

  public void onSelectFile(File file) {
    store.getDatasource().setLoadingHint(file);
    controller.reload();
  }

  @FXML
  public void onSelectFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(Localized.get("import.select.csv.file"));
    String homeDir = StandardSystemProperty.USER_HOME.value();
    fileChooser.setInitialDirectory(new File(homeDir));

    FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("*.csv", "*.csv");
    fileChooser.getExtensionFilters().add(extensionFilter);
    fileChooser.setSelectedExtensionFilter(extensionFilter);
    File file = fileChooser.showOpenDialog(selectFile.getScene().getWindow());
    if (file != null && file.exists()) {
      onSelectFile(file);
    }
  }

  @FXML
  public void onImport() {
    controller.save();
    controller.reload();
  }
}
