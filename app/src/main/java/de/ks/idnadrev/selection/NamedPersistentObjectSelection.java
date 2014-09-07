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
import de.ks.activity.ActivityController;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.group.LastTextChange;
import de.ks.i18n.Localized;
import de.ks.javafx.FxCss;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.reflection.PropertyPath;
import de.ks.validation.ValidationRegistry;
import de.ks.validation.validators.NamedEntityValidator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.criteria.Predicate;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class NamedPersistentObjectSelection<T extends NamedPersistentObject<T>> implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(NamedPersistentObjectSelection.class);
  @FXML
  protected TextField input;
  @FXML
  protected GridPane root;
  @FXML
  protected Button browse;
  protected Class<T> entityClass;
  protected TableView<T> tableView;

  protected SimpleObjectProperty<T> selectedValue = new SimpleObjectProperty<>();
  protected ActivityController controller = CDI.current().select(ActivityController.class).get();
  protected QueryConsumer<T, T> filter;
  private Dialog dialog;
  private CustomAutoCompletionBinding autoCompletion;
  private EventHandler<ActionEvent> onAction;
  private NamedPersistentObjectAutoCompletion<T> namedPersistentObjectAutoCompletion;
  private LastTextChange lastInputTextChange;

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
      String character = event.getCharacter();
      KeyCode code = event.getCode();
      String esc = String.valueOf((char) Ascii.ESC);
      if (code == KeyCode.ENTER || code == KeyCode.ESCAPE || character.equals("\n") || character.equals("\r") || character.equals(esc)) {
        submit();
      }
    });

    browse.disableProperty().bind(input.disabledProperty());

    tableView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null) {
        selectedValue.set(n);
      }
    });
    selectedValue.addListener((p, o, n) -> {
      if (n != null) {
        input.setText(n.getName());
        if (onAction != null) {
          onAction.handle(new ActionEvent());
        }
      }
    });
  }

  protected void findAndSetLastValue(String name) {
    T namedObject = PersistentWork.forName(entityClass, name);

    controller.getJavaFXExecutor().submit(() -> {
      setSelectedValue(namedObject);
    });
  }

  public NamedPersistentObjectSelection<T> from(Class<T> namedEntity) {
    from(namedEntity, null);
    return this;
  }

  public NamedPersistentObjectSelection<T> from(Class<T> namedEntity, QueryConsumer<T, T> filter) {
    this.entityClass = namedEntity;
    this.filter = filter;
    Platform.runLater(() -> {
      namedPersistentObjectAutoCompletion = new NamedPersistentObjectAutoCompletion<>(entityClass, filter);
      autoCompletion = new CustomAutoCompletionBinding(input, namedPersistentObjectAutoCompletion);
    });
    lastInputTextChange = new LastTextChange(input, controller.getExecutorService());
    lastInputTextChange.registerHandler(cf -> {
      cf.thenAccept(this::findAndSetLastValue);
    });
    return this;
  }

  public NamedPersistentObjectSelection<T> enableValidation() {
    ValidationRegistry validationRegistry = CDI.current().select(ValidationRegistry.class).get();
    Platform.runLater(() -> {
      validationRegistry.registerValidator(input, new NamedEntityValidator(entityClass));
    });
    return this;
  }

  @FXML
  void showBrowser() {
    ActivityExecutor executorService = controller.getExecutorService();
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();
    CompletableFuture.supplyAsync(this::readEntities, executorService)//
            .thenAcceptAsync(this::setTableItems, javaFXExecutor);

    StackPane content = new StackPane();
    content.getChildren().add(tableView);

    dialog = new Dialog(this.browse, Localized.get("select.namedEntity." + entityClass.getSimpleName()));
    dialog.setContent(tableView);
    dialog.show();
    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      dialog.getStylesheets().add(sheet);
    });

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
    dialog.hide();
    autoCompletion.hidePopup();
  }

  @FXML
  void onKeyPressed(KeyEvent event) {
    KeyCode code = event.getCode();
    if (code == KeyCode.ENTER) {
      if (onAction != null) {
        event.consume();
        onAction.handle(new ActionEvent());
        input.clear();
      }
    }
  }

  public void setOnAction(EventHandler<ActionEvent> handler) {
    this.onAction = handler;
  }

  public EventHandler<ActionEvent> getOnAction() {
    return onAction;
  }

  public boolean isSelectingProject() {
    return dialog != null && dialog.getWindow() != null && dialog.getWindow().isShowing();
  }

  public void hideBrowserBtn() {
    if (root.getChildren().contains(browse)) {
      root.getChildren().remove(browse);
    }
    root.setHgap(0.0D);
  }

  public T getSelectedValue() {
    return selectedValue.get();
  }

  public SimpleObjectProperty<T> selectedValueProperty() {
    return selectedValue;
  }

  public void setSelectedValue(T selectedValue) {
    this.selectedValue.set(selectedValue);
  }
}
