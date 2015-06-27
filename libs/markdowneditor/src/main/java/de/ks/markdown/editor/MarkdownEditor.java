/**
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.markdown.editor;

import de.ks.activity.ActivityController;
import de.ks.activity.initialization.ActivityCallback;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.i18n.Localized;
import de.ks.javafx.ScreenResolver;
import de.ks.markdown.viewer.MarkdownContent;
import de.ks.markdown.viewer.MarkdownViewer;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MarkdownEditor implements Initializable, ActivityCallback {
  public static CompletableFuture<DefaultLoader<Node, MarkdownEditor>> load(Consumer<StackPane> viewConsumer, Consumer<MarkdownEditor> controllerConsumer) {
    ActivityInitialization initialization = CDI.current().select(ActivityInitialization.class).get();
    return initialization.loadAdditionalControllerWithFuture(MarkdownEditor.class)//
      .thenApply(loader -> {
        viewConsumer.accept((StackPane) loader.getView());
        controllerConsumer.accept(loader.getController());
        return loader;
      });
  }

  private static final Logger log = LoggerFactory.getLogger(MarkdownEditor.class);

  @Inject
  ActivityController controller;
  @Inject
  ActivityInitialization initialization;
  @FXML
  protected TextArea editor;
  @FXML
  protected Tab previewTab;
  @FXML
  protected Button help;
  @FXML
  protected GridPane mainPane;
  @FXML
  protected TabPane tabPane;
  @FXML
  protected StackPane root;
  @FXML
  protected HBox editorCommandPane;
  @FXML
  protected StackPane editorContainer;
  @FXML
  protected TextArea plainHtml;
  protected File lastFile;

  protected final SimpleStringProperty text = new SimpleStringProperty();
  protected LastExecutionGroup<String> renderGroup;
  protected Button insertImage = null;
  protected boolean focusOnEditor = true;

  protected Stage previewPopupStage;

  protected MarkdownViewer preview;
  protected MarkdownViewer popupPreview;
  protected Node previewNode;
  protected Node popupPreviewNode;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initializePreview();
    initializePopupPreview();

    renderGroup = new LastExecutionGroup<>("markdownrender", 500, controller.getExecutorService());

    text.bindBidirectional(editor.textProperty());

    editor.textProperty().addListener((p, o, n) -> {
      if (n != null) {
        renderGroup.schedule(() -> n)//
          .thenApplyAsync(s -> {
            return s;
          }, controller.getExecutorService())//
          .thenAcceptAsync(s -> {
            if (previewTab.isSelected()) {
              preview.clear();
              preview.showDirect(s);
            } else {
              preview.preload(Collections.singletonList(new MarkdownContent(MarkdownViewer.DEFAULT, s)));
            }
            if (previewPopupStage != null) {
              popupPreview.clear();
              popupPreview.showDirect(s);
            }
          }, controller.getJavaFXExecutor());
      }
    });


    tabPane.focusedProperty().addListener((p, o, n) -> {
      if (n) {
        if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
          if (focusOnEditor) {
            editor.requestFocus();
          }
        }
      } else {
        focusOnEditor = true;
      }
    });

    tabPane.getSelectionModel().selectedIndexProperty().addListener((p, o, n) -> {
      if (n != null && n.intValue() == 1) {
        controller.getJavaFXExecutor().submit(() -> preview.requestFocus());
        preview.show(new MarkdownContent(MarkdownViewer.DEFAULT, editor.getText()));
      }
      if (o == null || n == null) {
        return;
      }
      if (o.intValue() != 0 && n.intValue() == 0) {
        controller.getJavaFXExecutor().submit(() -> editor.requestFocus());
      }
    });

    editor.setOnKeyPressed(e -> {
      KeyCode code = e.getCode();
      if (e.getCode() == KeyCode.P && e.isControlDown()) {
        showPreviewPopup();
        e.consume();
      }
    });
  }

  protected void initializePreview() {
    DefaultLoader<Node, MarkdownViewer> previewLoader = initialization.loadAdditionalController(MarkdownViewer.class);
    previewNode = previewLoader.getView();
    previewTab.setContent(previewNode);
    preview = previewLoader.getController();
    plainHtml.textProperty().bind(preview.currentHtmlProperty());
  }

  protected void initializePopupPreview() {
    DefaultLoader<Node, MarkdownViewer> previewLoader = initialization.loadAdditionalController(MarkdownViewer.class);
    popupPreviewNode = previewLoader.getView();
    popupPreview = previewLoader.getController();
  }

  @FXML
  void showHelp() {
    String title = Localized.get("help");
    title = StringUtils.remove(title, "_");

    new Thread(() -> {
      Desktop desktop = Desktop.getDesktop();
      URI uri = URI.create("http://daringfireball.net/projects/markdown/syntax");
      try {
        desktop.browse(uri);
      } catch (IOException e) {
        log.error("Could not browse {}", uri, e);
      }
    }).start();
  }

  @FXML
  void showPreviewPopup() {
    if (previewPopupStage == null) {
      String title = Localized.get("markdown.preview");

      previewPopupStage = new Stage();
      previewPopupStage.setTitle(title);
      Scene scene = new Scene(new StackPane(popupPreviewNode));
      scene.setOnKeyReleased(e -> {
        if (e.getCode() == KeyCode.ESCAPE) {
          previewPopupStage.close();
        }
      });
      previewPopupStage.setScene(scene);

      Rectangle2D bounds = new ScreenResolver().getScreenToShow().getBounds();
      previewPopupStage.setX(bounds.getMinX());
      previewPopupStage.setY(bounds.getMinY());
      previewPopupStage.setWidth(bounds.getWidth());
      previewPopupStage.setHeight(bounds.getHeight());

      previewPopupStage.initModality(Modality.NONE);
      previewPopupStage.setOnShowing(e -> {
        popupPreview.showDirect(getText());
      });
      previewPopupStage.setOnCloseRequest(e -> this.previewPopupStage = null);
      previewPopupStage.show();
    }
  }

  public SimpleStringProperty textProperty() {
    return text;
  }

  public String getText() {
    return text.getValueSafe();
  }

  public void setText(String text) {
    this.editor.setText(text);
  }

  public TextArea getEditor() {
    return editor;
  }

  public void selectPreview() {
    this.tabPane.getSelectionModel().select(1);
  }

  public void selectEditor() {
    this.tabPane.getSelectionModel().select(0);
  }

  public WebView getPreview() {
    return preview.getWebView();
  }

  @Override
  public void onSuspend() {
    controller.getJavaFXExecutor().invokeInJavaFXThread(() -> {
      if (previewPopupStage != null) {
        previewPopupStage.close();
      }
      return null;
    });
  }

  @Override
  public void onStop() {
    onSuspend();
  }
}
