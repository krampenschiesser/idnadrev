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
import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.tag.TagContainer;
import de.ks.standbein.BaseController;
import de.ks.standbein.reflection.PropertyPath;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
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

public class InformationListView extends BaseController<List<Information>> {
  @FXML
  protected GridPane root;
  @FXML
  protected TextField nameSearch;
  @FXML
  protected TagContainer tagContainerController;
  @FXML
  protected TableView<Information> informationList;
  @FXML
  protected TableColumn<Information, String> nameColumn;
  @FXML
  protected TableColumn<Information, String> creationDateColumn;

  protected LastTextChange lastTextChange;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    lastTextChange = new LastTextChange(nameSearch, controller.getExecutorService());
    lastTextChange.registerHandler(cf -> triggerReload());
    informationList.setItems(FXCollections.observableArrayList());

    tagContainerController.getCurrentTags().addListener((SetChangeListener<String>) change -> triggerReload());

    nameColumn.setCellValueFactory(new PropertyValueFactory<>(PropertyPath.property(Information.class, NamedEntity::getName)));
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
  protected void onRefresh(List<Information> model) {
    super.onRefresh(model);

    ObservableList<Information> items = informationList.getItems();
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

    InformationLoadingHint loadingHint = new InformationLoadingHint(name);
    Set<String> tags = tagContainerController.getCurrentTags();
    loadingHint.setTags(tags);
    return loadingHint;
  }

  public TableView<Information> getInformationList() {
    return informationList;
  }
}
