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
package de.ks.idnadrev.information.uml;

import de.ks.BaseController;
import de.ks.executor.group.LastTextChange;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Screen;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class UmlDiagramController extends BaseController<UmlDiagramInfo> {
  private static final Logger log = LoggerFactory.getLogger(UmlDiagramController.class);
  @FXML
  protected Button saveImage;
  @FXML
  protected Button fullscreen;
  @FXML
  protected StackPane previewContainer;
  @FXML
  protected TextField name;

  @FXML
  protected SplitPane splitPane;
  //  @FXML
//  protected ComboBox<UmlDiagramType> diagramType;
  @FXML
  protected TextArea content;
  @FXML
  protected Button saveBtn;

  protected WebView webView;
  protected WebView fullScreenWebView;

  private LastTextChange lastTextChange;
  protected boolean showFullScreen = false;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CompletableFuture.supplyAsync(() -> new WebView(), controller.getJavaFXExecutor()).thenAccept(view -> {
      webView = view;
      webView.setMinSize(100, 100);
      webView.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      previewContainer.getChildren().add(webView);
    });
    CompletableFuture.supplyAsync(() -> new WebView(), controller.getJavaFXExecutor()).thenAccept(view -> {
      fullScreenWebView = view;
      fullScreenWebView.setMinSize(100, 100);
      fullScreenWebView.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
    });

    StringProperty nameProperty = store.getBinding().getStringProperty(UmlDiagramInfo.class, t -> t.getName());
    name.textProperty().bindBidirectional(nameProperty);

    StringProperty contentProperty = store.getBinding().getStringProperty(UmlDiagramInfo.class, t -> t.getContent());
    content.textProperty().bindBidirectional(contentProperty);

    validationRegistry.registerValidator(name, new NotEmptyValidator());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(UmlDiagramInfo.class, t -> t.getId() == store.<TextInfo>getModel().getId()));

    saveBtn.disableProperty().bind(validationRegistry.invalidProperty());


    lastTextChange = new LastTextChange(content, 750, controller.getExecutorService());
    lastTextChange.registerHandler(f -> {
      f.thenApplyAsync(s -> genereateSvg(s, getFullScreenWidth(), getFullScreenImagePath()), controller.getExecutorService()).thenAcceptAsync(file -> {
        if (showFullScreen) {
          showRenderedFile(file, fullScreenWebView);
        }
      }, controller.getJavaFXExecutor());

      f.thenApply(s -> genereateSvg(s, webView.getWidth(), getImagePath())).thenAcceptAsync(file -> showRenderedFile(file, webView), controller.getJavaFXExecutor());
    });

    webView.widthProperty().addListener((p, o, n) -> {
      lastTextChange.trigger();
    });
  }

  private void showRenderedFile(File file, WebView view) {
    if (file == null) {
      return;
    }
    try {
      URL url = file.toURI().toURL();
      view.getEngine().load(url.toExternalForm());
    } catch (MalformedURLException e) {
      log.error("Could not load {}", file, e);
    }
  }

  private File genereateSvg(String uml, double width, Path path) {
    StringBuilder builder = new StringBuilder();
    builder.append("@startuml\n");
    builder.append("scale ");
    builder.append((int) width);
    builder.append(" width \n");
    builder.append(uml);
    builder.append("\n@enduml");

    try {
      try (FileOutputStream outStream = new FileOutputStream(path.toFile())) {
        SourceStringReader reader = new SourceStringReader(builder.toString());
        String desc = reader.generateImage(outStream, new FileFormatOption(FileFormat.PNG));

        log.info(desc);
      }
      return path.toFile();
    } catch (Exception e) {
      log.error("Could not create uml diagram", e);
      return null;
    }
  }

  private Path getImagePath() {
    return Paths.get(System.getProperty("java.io.tmpdir"), "idnadrev_uml_editor.png");
  }

  private Path getFullScreenImagePath() {
    return Paths.get(System.getProperty("java.io.tmpdir"), "idnadrev_uml_editor_fullscreen.png");
  }

  @Override
  protected void onRefresh(UmlDiagramInfo model) {
    super.onRefresh(model);
  }

  @FXML
  protected void onSave() {
    controller.save();
  }

  @FXML
  protected void onSaveImage() {
//    controller.save();
  }

  private double getFullScreenWidth() {
    Screen primary = Screen.getPrimary();
    Rectangle2D visualBounds = primary.getVisualBounds();
    return visualBounds.getWidth();
  }

  @FXML
  public void onShowFullScreen() {
    Screen primary = Screen.getPrimary();
    Rectangle2D visualBounds = primary.getVisualBounds();

    this.showFullScreen = true;
    CompletableFuture<File> future = CompletableFuture.supplyAsync(() -> {
      Path path = getFullScreenImagePath();
      if (path.toFile().exists()) {
        return path.toFile();
      } else {
        return null;
      }
    }, controller.getExecutorService());

    future.thenAcceptAsync(file -> showRenderedFile(file, fullScreenWebView), controller.getJavaFXExecutor());

    Popup popup = new Popup();
    StackPane pane = new StackPane();
    pane.setPrefWidth(visualBounds.getWidth());
    pane.setPrefHeight(visualBounds.getHeight());
    pane.getChildren().add(fullScreenWebView);

    popup.getContent().add(pane);
    popup.setX(0);
    popup.setY(0);
    popup.setWidth(visualBounds.getWidth());
    popup.setHeight(visualBounds.getHeight());
    popup.setOnHiding(e -> showFullScreen = false);
    popup.show(fullscreen.getScene().getWindow());
  }
}