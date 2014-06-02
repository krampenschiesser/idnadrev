/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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
package de.ks.beagle.selection;

import de.ks.activity.ActivityController;
import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.SuspendablePooledExecutorService;
import de.ks.i18n.Localized;
import de.ks.javafx.FxCss;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.PropertyPath;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.criteria.Predicate;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class NamedPersistentObjectSelection<T extends NamedPersistentObject<T>> implements Initializable {
  @FXML
  protected TextField input;
  @FXML
  protected GridPane root;
  @FXML
  protected Button browse;
  protected Class<T> entityClass;
  protected TableView<T> tableView;

  protected SimpleObjectProperty<T> selectedValue = new SimpleObjectProperty<>();
  protected Stage stage;
  protected ActivityController controller = CDI.current().select(ActivityController.class).get();
  protected QueryConsumer<T> filter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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
      KeyCode code = event.getCode();
      if (code == KeyCode.ENTER) {
        submit();
      }
    });

    browse.disableProperty().bind(input.disabledProperty());

    selectedValue.bind(tableView.getSelectionModel().selectedItemProperty());
    selectedValue.addListener((p, o, n) -> {
      if (n != null) {
        input.setText(n.getName());
      }
    });
  }

  public void from(Class<T> namedEntity) {
    this.entityClass = namedEntity;
  }

  public void from(Class<T> namedEntity, QueryConsumer<T> consumer) {
    this.entityClass = namedEntity;
    this.filter = consumer;
  }

  @FXML
  void showBrowser() {
    SuspendablePooledExecutorService executorService = controller.getCurrentExecutorService();
    CompletableFuture.supplyAsync(this::readEntities, executorService)//
            .thenAcceptAsync(this::setTableItems, new JavaFXExecutorService());

    StackPane content = new StackPane();
    content.getChildren().add(tableView);
    Scene scene = new Scene(content);
    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });

    stage = new Stage();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setScene(scene);
    stage.show();
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

  public BooleanProperty disableProperty() {
    return input.disableProperty();
  }

  public TextField getInput() {
    return input;
  }

  protected void submit() {
    stage.hide();
  }
}
