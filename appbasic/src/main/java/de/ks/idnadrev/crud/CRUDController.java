/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.crud;

import de.ks.idnadrev.menu.GlobalMenu;
import de.ks.idnadrev.util.ButtonHelper;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.context.ActivityContext;
import de.ks.standbein.activity.initialization.ActivityInitialization;
import de.ks.standbein.application.fxml.DefaultLoader;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.reactfx.EventStreams;
import org.reactfx.Subscription;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CRUDController extends BaseController<Object> {

  private Subscription subscription;

  public static CompletableFuture<DefaultLoader<Node, CRUDController>> load(ActivityInitialization initialization, Consumer<GridPane> viewConsumer, Consumer<CRUDController> controllerConsumer) {
    return initialization.loadAdditionalControllerWithFuture(CRUDController.class)//
      .thenApply(loader -> {
        CRUDController controller = loader.getController();
        controllerConsumer.accept(controller);
        viewConsumer.accept(controller.getRoot());
        return loader;
      });
  }

  @FXML
  protected Button delete;
  @FXML
  protected Button menu;
  @FXML
  protected Button back;
  @FXML
  protected Button save;
  @FXML
  protected GridPane root;
  @FXML
  protected HBox centerButtonContainer;
  @FXML
  protected HBox leftButtonContainer;

  @Inject
  ActivityContext context;
  @Inject
  ButtonHelper buttonHelper;
  @Inject
  GlobalMenu globalMenu;

  protected SimpleBooleanProperty backDisabled = new SimpleBooleanProperty();
  protected SimpleBooleanProperty backDisabledInternal = new SimpleBooleanProperty();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    back.setOnAction(e -> controller.stopCurrent());
    back.disableProperty().bind(backDisabled.or(backDisabledInternal));
    save.disableProperty().bind(validationRegistry.invalidProperty());
    menu.setOnAction(e -> globalMenu.show(menu));
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    if (context.getActivities().size() == 2) {
      backDisabledInternal.set(true);
    } else {
      backDisabledInternal.set(false);
    }


    subscription = EventStreams.eventsOf(menu.getScene(), KeyEvent.KEY_RELEASED).filter(e -> e.getCode() == KeyCode.F1).subscribe(e -> globalMenu.show(menu));
  }

  @Override
  public void onSuspend() {
    if (subscription != null) {
      subscription.unsubscribe();
    }
  }

  @Override
  public void onStop() {
    onSuspend();
  }

  public Button getDeleteButton() {
    return delete;
  }

  public Button getBackButton() {
    return back;
  }

  public Button getSaveButton() {
    return save;
  }

  public GridPane getRoot() {
    return root;
  }

  public HBox getCenterButtonContainer() {
    return centerButtonContainer;
  }

  public boolean getBackDisabled() {
    return backDisabled.get();
  }

  public SimpleBooleanProperty backDisabledProperty() {
    return backDisabled;
  }

  public void setBackDisabled(boolean backDisabled) {
    this.backDisabled.set(backDisabled);
  }
}
