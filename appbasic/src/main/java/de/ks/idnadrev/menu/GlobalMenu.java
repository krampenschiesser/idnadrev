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
package de.ks.idnadrev.menu;

import com.google.inject.Injector;
import de.ks.idnadrev.util.ButtonHelper;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.imagecache.Images;
import de.ks.standbein.menu.MenuEntry;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.PopupWindow;
import org.reactfx.EventStreams;
import org.reactfx.Subscription;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Singleton
public class GlobalMenu {
  private final List<MenuEntry> menuEntries;
  private final ButtonHelper buttonHelper;
  private final Localized localized;
  private final Images images;
  private final AtomicBoolean showing = new AtomicBoolean();
  private Injector injector;

  @Inject
  public GlobalMenu(Set<MenuEntry> entries, ButtonHelper buttonHelper, Localized localized, Images images, Injector injector) {
    this.buttonHelper = buttonHelper;
    this.localized = localized;
    this.images = images;
    this.injector = injector;
    menuEntries = entries.stream().filter(e -> e.getPath().startsWith("/main")).collect(Collectors.toList());
    menuEntries.sort(Comparator.comparingInt(MenuEntry::getOrder));
  }

  private StackPane createMenu() {
    StackPane stackPane = new StackPane();
    GridPane gridPane = new GridPane();
    gridPane.setPadding(new Insets(10));
    gridPane.setHgap(15);
    gridPane.setVgap(20);
    gridPane.getStyleClass().add("globalMenu");
    stackPane.getChildren().add(gridPane);

    Map<String, List<MenuEntry>> path2Items = new LinkedHashMap<>();
    menuEntries.stream().sequential().forEach(e -> path2Items.computeIfAbsent(e.getPath(), s -> new ArrayList<>()).add(e));

    int row = 0;
    for (Map.Entry<String, List<MenuEntry>> entry : path2Items.entrySet()) {
      List<MenuEntry> entries = entry.getValue();
      String key = entry.getKey();
      Image image = images.get(key.substring(key.lastIndexOf("/")) + ".png");
      if (image != null) {
        ImageView imageView = new ImageView(image);
        gridPane.add(imageView, 0, row);
      }
      String title = localized.get(key);
      Label label = new Label(title + ":");
      label.getStyleClass().add("editorViewLabel");
      gridPane.add(label, 1, row);
      GridPane.setValignment(label, VPos.CENTER);

      HBox hBox = new HBox();
      hBox.setSpacing(5);
      hBox.setAlignment(Pos.CENTER_LEFT);
      for (MenuEntry menuEntry : entries) {
        Button button = createButton(menuEntry);

        hBox.getChildren().add(button);
      }
      gridPane.add(hBox, 2, row);
      GridPane.setValignment(hBox, VPos.CENTER);
      row++;
    }
    return stackPane;
  }

  private Button createButton(MenuEntry menuEntry) {
    String name = menuEntry.getName();
    String translation = localized.get(name);
    Button button;
    if (menuEntry.getIconPath() != null) {
      button = buttonHelper.createImageButton(translation, menuEntry.getIconPath(), 36);
    } else {
      button = new Button(translation);
    }
    button.setPrefWidth(150);
    button.setPrefHeight(50);
    button.setOnAction(e -> menuEntry.getAction().accept(injector));
    return button;
  }

  public void show(Node parent) {
    boolean wasNotShowing = showing.compareAndSet(false, true);
    if (wasNotShowing) {
      Scene scene = parent.getScene();
      Parent root = scene.getRoot();

      root.getStyleClass().add("fadingContent");
      PopupWindow popupWindow = new PopupControl();
      popupWindow.setAutoHide(true);
      popupWindow.getScene().setRoot(createMenu());
      Point2D point2D = parent.localToScreen(-2, -2);
      popupWindow.show(parent, point2D.getX(), point2D.getY());

      Subscription subscription = EventStreams.changesOf(parent.sceneProperty()).filter(s -> s.getNewValue() == null).subscribe(n -> popupWindow.hide());
      popupWindow.setOnHiding(e -> {
        showing.set(false);
        root.getStyleClass().remove("fadingContent");
        subscription.unsubscribe();
      });
    }
  }
}
