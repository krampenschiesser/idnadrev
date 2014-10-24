/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.information.view;

import de.ks.BaseController;
import de.ks.executor.group.LastTextChange;
import de.ks.i18n.Localized;
import de.ks.idnadrev.category.CategorySelection;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.idnadrev.tag.TagContainer;
import de.ks.reflection.PropertyPath;
import de.ks.validation.validators.NamedEntityValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class InformationListView extends BaseController<List<InformationPreviewItem>> {
  public static final int CELL_SIZE = 30;

  @FXML
  protected TextField nameSearch;
  @FXML
  protected TagContainer tagContainerController;
  @FXML
  protected CategorySelection categorySelectionController;
  @FXML
  protected ComboBox<Class<? extends Information<?>>> typeCombo;
  @FXML
  protected TableView<InformationPreviewItem> informationList;
  @FXML
  protected TableColumn<InformationPreviewItem, String> nameColumn;
  @FXML
  protected TableColumn<InformationPreviewItem, String> typeColumn;
  @FXML
  protected TableColumn<InformationPreviewItem, String> creationDateColumn;

  //  protected final SimpleIntegerProperty visibleItemCount = new SimpleIntegerProperty();
  protected final Map<String, Class<?>> comboValues = new HashMap<>();
  protected LastTextChange lastTextChange;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    lastTextChange = new LastTextChange(nameSearch, controller.getExecutorService());
    lastTextChange.registerHandler(cf -> triggerReload());
    informationList.setItems(FXCollections.observableArrayList());

    typeCombo.setItems(FXCollections.observableArrayList(NoInfo.class, ChartInfo.class, TextInfo.class, UmlDiagramInfo.class));

    typeCombo.setConverter(new StringConverter<Class<? extends Information<?>>>() {
      @Override
      public String toString(Class<? extends Information<?>> c) {
        if (c.equals(NoInfo.class)) {
          return "";
        } else {
          String translation = Localized.get(c.getSimpleName());
          return translation;
        }
      }

      @Override
      public Class<? extends Information<?>> fromString(String string) {
        return null;
      }
    });
    typeCombo.getSelectionModel().select(0);
    typeCombo.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> triggerReload());
    tagContainerController.getCurrentTags().addListener((SetChangeListener<String>) change -> triggerReload());
    categorySelectionController.selectedValueProperty().addListener((observable, oldValue, newValue) -> triggerReload());

    nameColumn.setCellValueFactory(new PropertyValueFactory<>(PropertyPath.property(InformationPreviewItem.class, i -> i.getName())));
    typeColumn.setCellValueFactory(cd -> {
      InformationPreviewItem value = cd.getValue();
      String translation = Localized.get(value.getType().getSimpleName());
      return new SimpleStringProperty(translation);
    });
    creationDateColumn.setCellValueFactory(cd -> {
      LocalDate date = cd.getValue().getCreationTime().toLocalDate();
      String format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(date);
      return new SimpleStringProperty(format);
    });

    validationRegistry.registerValidator(tagContainerController.getInput(), new NamedEntityValidator(Tag.class));
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
//    int itemsPerPage = visibleItemCount.get();
//    int firstResult = pager.getCurrentPageIndex() * itemsPerPage;
    int itemsPerPage = -1;
    int firstResult = -1;
    Class<? extends Information<?>> infoClass = typeCombo.getSelectionModel().getSelectedItem();
    infoClass = infoClass.equals(NoInfo.class) ? null : infoClass;
    String name = nameSearch.textProperty().getValueSafe().toLowerCase(Locale.ROOT).trim();

    InformationLoadingHint loadingHint = new InformationLoadingHint(firstResult, itemsPerPage, infoClass, name, categorySelectionController.getSelectedValue());
    Set<String> tags = tagContainerController.getCurrentTags();
    loadingHint.setTags(tags);
    return loadingHint;
  }

  protected static class NoInfo extends Information<NoInfo> {
    //dummy because javafx can't handle null values in observable list
  }
}
