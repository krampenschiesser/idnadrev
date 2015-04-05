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
import de.ks.idnadrev.cost.bookingview.BookingViewTableController;
import de.ks.idnadrev.entity.cost.Booking;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BookingFromCSVController extends BaseController<List<Booking>> {

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

  @FXML
  public void onImport() {

  }

  @FXML
  public void onSelectFile() {

  }

}
