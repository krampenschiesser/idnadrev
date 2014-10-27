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
package de.ks.selection;

import de.ks.activity.ActivityController;
import de.ks.executor.group.LastTextChange;
import de.ks.i18n.Localized;
import de.ks.javafx.FxCss;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.QueryConsumer;
import de.ks.persistence.entity.NamedPersistentObject;
import de.ks.validation.ValidationRegistry;
import de.ks.validation.validators.NamedEntityValidator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseNamedPersistentObjectSelection<T extends NamedPersistentObject<T>> implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(BaseNamedPersistentObjectSelection.class);
  @FXML
  protected TextField input;
  @FXML
  protected GridPane root;
  @FXML
  protected Button browse;
  protected Class<T> entityClass;

  protected SimpleObjectProperty<T> selectedValue = new SimpleObjectProperty<>();
  protected ActivityController controller = CDI.current().select(ActivityController.class).get();
  protected QueryConsumer<T, T> filter;
  protected Stage dialog;
  private CustomAutoCompletionBinding autoCompletion;
  private EventHandler<ActionEvent> onAction;
  private NamedPersistentObjectAutoCompletion<T> namedPersistentObjectAutoCompletion;
  private LastTextChange lastInputTextChange;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    browse.disableProperty().bind(input.disabledProperty());
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

  public BaseNamedPersistentObjectSelection<T> from(Class<T> namedEntity) {
    from(namedEntity, null);
    return this;
  }

  public BaseNamedPersistentObjectSelection<T> from(Class<T> namedEntity, QueryConsumer<T, T> filter) {
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

  public BaseNamedPersistentObjectSelection<T> enableValidation() {
    ValidationRegistry validationRegistry = CDI.current().select(ValidationRegistry.class).get();
    Platform.runLater(() -> {
      validationRegistry.registerValidator(input, new NamedEntityValidator(entityClass));
    });
    return this;
  }

  @FXML
  protected void showBrowser() {
    String title = Localized.get("select.namedEntity." + entityClass.getSimpleName());

    dialog = new Stage();
    dialog.setTitle(title);
    Scene scene = new Scene(new StackPane(getBrowseNode()));
    dialog.setScene(scene);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setAlwaysOnTop(true);

    dialog.show();
    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });
  }

  public BooleanProperty disableProperty() {
    return input.disableProperty();
  }

  public TextField getInput() {
    return input;
  }

  public void hidePopup() {
    if (dialog != null) {
      dialog.hide();
    }
    if (autoCompletion != null) {
      autoCompletion.hidePopup();
    }
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
    return dialog != null && dialog.isShowing();
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

  protected abstract Node getBrowseNode();
}
