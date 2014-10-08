/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks.idnadrev.selection;

import com.google.common.base.Ascii;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.executor.JavaFXExecutorService;
import de.ks.i18n.Localized;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.PropertyPath;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.Predicate;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class NamedPersistentObjectSelection<T extends NamedPersistentObject<T>> extends BaseNamedPersistentObjectSelection<T> {
  private static final Logger log = LoggerFactory.getLogger(NamedPersistentObjectSelection.class);

  protected TableView<T> tableView;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    tableView = new TableView<>();
    TableColumn<T, String> column = new TableColumn<>(Localized.get("name"));
    column.setCellValueFactory(param -> {
      T value = param.getValue();
      return new SimpleStringProperty(value.getName());
    });
    column.prefWidthProperty().bind(tableView.widthProperty());
    tableView.getColumns().add(column);
    tableView.setItems(FXCollections.observableArrayList());

    tableView.setOnMouseClicked((event) -> {
      if (event.getClickCount() > 1) {
        submit();
      }
    });
    tableView.setOnKeyTyped((event) -> {
      String character = event.getCharacter();
      KeyCode code = event.getCode();
      String esc = String.valueOf((char) Ascii.ESC);
      if (code == KeyCode.ENTER || code == KeyCode.ESCAPE || character.equals("\n") || character.equals("\r") || character.equals(esc)) {
        submit();
      }
    });

    tableView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null) {
        selectedValue.set(n);
      }
    });
  }

  @FXML
  protected void showBrowser() {
    ActivityExecutor executorService = controller.getExecutorService();
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();
    CompletableFuture.supplyAsync(this::readEntities, executorService)//
            .thenAcceptAsync(this::setTableItems, javaFXExecutor);

    super.showBrowser();

    tableView.requestFocus();
  }

  private void setTableItems(List<T> newItems) {
    this.tableView.getItems().clear();
    this.tableView.getItems().addAll(newItems);
  }

  protected List<T> readEntities() {
    String nameProperty = PropertyPath.property(NamedPersistentObject.class, (o) -> o.getName());
    String name = (input.textProperty().getValueSafe() + "%")//
            .replaceAll("\\*", "%")//
            .replaceAll("\\?", "_")//
            .toLowerCase(Locale.getDefault());

    return PersistentWork.from(entityClass, (root, query, builder) -> {
      Predicate like = builder.like(builder.lower(root.get(nameProperty)), name);
      if (filter != null) {
        filter.accept(root, query, builder);
        query.where(query.getRestriction(), like);
      } else {
        query.where(like);
      }
    }, null);
  }

  @Override
  protected Node getBrowseNode() {
    return tableView;
  }
}
