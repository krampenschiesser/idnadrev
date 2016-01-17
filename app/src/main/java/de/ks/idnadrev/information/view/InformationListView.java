/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.information.view;

import de.ks.executor.group.LastTextChange;
import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.tag.TagContainer;
import de.ks.standbein.BaseController;
import de.ks.standbein.reflection.PropertyPath;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class InformationListView extends BaseController<List<InformationPreviewItem>> {
  public static final int CELL_SIZE = 30;
  @FXML
  protected GridPane root;
  @FXML
  protected TextField nameSearch;
  @FXML
  protected TagContainer tagContainerController;
  //  @FXML
//  protected CategorySelection categorySelectionController;
  @FXML
  protected Label typeLabel;
  @FXML
  protected TableView<InformationPreviewItem> informationList;
  @FXML
  protected TableColumn<InformationPreviewItem, String> nameColumn;
  @FXML
  protected TableColumn<InformationPreviewItem, String> typeColumn;
  @FXML
  protected TableColumn<InformationPreviewItem, String> creationDateColumn;

  protected LastTextChange lastTextChange;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    lastTextChange = new LastTextChange(nameSearch, controller.getExecutorService());
    lastTextChange.registerHandler(cf -> triggerReload());
    informationList.setItems(FXCollections.observableArrayList());

    tagContainerController.getCurrentTags().addListener((SetChangeListener<String>) change -> triggerReload());
//    categorySelectionController.selectedValueProperty().addListener((observable, oldValue, newValue) -> triggerReload());

    nameColumn.setCellValueFactory(new PropertyValueFactory<>(PropertyPath.property(InformationPreviewItem.class, i -> i.getName())));
    typeColumn.setCellValueFactory(cd -> {
      InformationPreviewItem value = cd.getValue();
      String translation = localized.get(value.getType().getSimpleName());
      return new SimpleStringProperty(translation);
    });
    creationDateColumn.setCellValueFactory(cd -> {
      LocalDate date = cd.getValue().getCreationTime().toLocalDate();
      String format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(date);
      return new SimpleStringProperty(format);
    });
// FIXME: 12/17/15
//    validationRegistry.registerValidator(tagContainerController.getInput(), new NamedEntityValidator(Tag.class));
    tagContainerController.setReadOnly(true);
  }

  @Override
  protected void onRefresh(List<InformationPreviewItem> model) {
    super.onRefresh(model);

    ObservableList<InformationPreviewItem> items = informationList.getItems();
    items.clear();
    items.addAll(model);
    informationList.sort();
  }

  private void triggerReload() {
    store.getDatasource().setLoadingHint(createLoadingHint());
    controller.reload();
  }

  private InformationLoadingHint createLoadingHint() {
    String name = nameSearch.textProperty().getValueSafe().toLowerCase(Locale.ROOT).trim();

    InformationLoadingHint loadingHint = new InformationLoadingHint(Information.class, name);
    Set<String> tags = tagContainerController.getCurrentTags();
    loadingHint.setTags(tags);
    return loadingHint;
  }

  public void setFixedTypeFilter(Class<Information> type) {
    controller.getJavaFXExecutor().submit(() -> {
      typeLabel.setVisible(false);
      root.getRowConstraints().get(1).setPrefHeight(0.0F);
      root.getRowConstraints().get(1).setMinHeight(0.0F);
    });
  }

  public TableView<InformationPreviewItem> getInformationList() {
    return informationList;
  }
}
