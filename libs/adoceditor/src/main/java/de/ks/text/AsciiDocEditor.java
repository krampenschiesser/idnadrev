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
package de.ks.text;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.initialization.ActivityCallback;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.initialization.DatasourceCallback;
import de.ks.application.fxml.DefaultLoader;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.i18n.Localized;
import de.ks.javafx.FxCss;
import de.ks.javafx.ScreenResolver;
import de.ks.text.command.AsciiDocEditorCommand;
import de.ks.text.view.AsciiDocContent;
import de.ks.text.view.AsciiDocViewer;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AsciiDocEditor implements Initializable, DatasourceCallback<Object>, ActivityCallback {
  public static CompletableFuture<DefaultLoader<Node, AsciiDocEditor>> load(Consumer<StackPane> viewConsumer, Consumer<AsciiDocEditor> controllerConsumer) {
    ActivityInitialization initialization = CDI.current().select(ActivityInitialization.class).get();
    return initialization.loadAdditionalControllerWithFuture(AsciiDocEditor.class)//
      .thenApply(loader -> {
        viewConsumer.accept((StackPane) loader.getView());
        controllerConsumer.accept(loader.getController());
        return loader;
      });
  }

  private static final Logger log = LoggerFactory.getLogger(AsciiDocEditor.class);

  @Inject
  @FxCss
  Instance<String> stylesheets;
  @Inject
  AsciiDocParser parser;
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
  protected Button saveToFileButton;
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
  protected TextField searchField;
  @FXML
  protected TextArea plainHtml;
  protected File lastFile;

  protected Dialog helpDialog;
  protected WebView helpView;
  protected final SimpleStringProperty text = new SimpleStringProperty();
  protected LastExecutionGroup<String> renderGroup;
  protected Button insertImage = null;
  protected boolean focusOnEditor = true;
  protected final Map<Class<?>, AsciiDocEditorCommand> commands = new HashMap<>();

  protected Stage previewPopupStage;
  protected volatile PersistentStoreBack persistentStoreBack;
  protected volatile LastSearch lastSearch = null;

  protected AsciiDocViewer preview;
  protected AsciiDocViewer popupPreview;
  protected Node previewNode;
  protected Node popupPreviewNode;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    initializePreview();
    initializePopupPreview();

    renderGroup = new LastExecutionGroup<>("adocrender", 500, controller.getExecutorService());

    CompletableFuture.supplyAsync(() -> new WebView(), controller.getJavaFXExecutor())//
      .thenAccept(webView -> {
        helpView = webView;
        helpView.getEngine().load("http://powerman.name/doc/asciidoc");
      });
    text.bindBidirectional(editor.textProperty());

    editor.textProperty().addListener((p, o, n) -> {
      if (n != null) {
        renderGroup.schedule(() -> n)//
          .thenApplyAsync(s -> {
            storeBack(s);
            return s;
          }, controller.getExecutorService())//
          .thenAcceptAsync(s -> {
            if (previewTab.isSelected()) {
              preview.clear();
              preview.showDirect(s);
            } else {
              preview.preload(Collections.singletonList(new AsciiDocContent(AsciiDocViewer.DEFAULT, s)));
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
        preview.show(new AsciiDocContent(AsciiDocViewer.DEFAULT, editor.getText()));
      }
      if (o == null || n == null) {
        return;
      }
      if (o.intValue() != 0 && n.intValue() == 0) {
        controller.getJavaFXExecutor().submit(() -> editor.requestFocus());
      }
    });
    addCommands();

    editor.setOnKeyPressed(e -> {
      KeyCode code = e.getCode();
      if (code == KeyCode.S && e.isControlDown()) {
        saveToFile();
        e.consume();
      }
      if (e.getCode() == KeyCode.P && e.isControlDown()) {
        showPreviewPopup();
        e.consume();
      }
      if (e.getCode() == KeyCode.F && e.isControlDown()) {
        showSearchField();
        e.consume();
      }
    });
    searchField.setVisible(false);
    searchField.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        searchForText();
        e.consume();
      }
    });
    searchField.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        if (searchField.textProperty().getValueSafe().trim().isEmpty()) {
          searchField.setVisible(false);
          editor.requestFocus();
        } else {
          searchField.setText("");
        }
      }
    });
  }

  protected void initializePreview() {
    DefaultLoader<Node, AsciiDocViewer> previewLoader = initialization.loadAdditionalController(AsciiDocViewer.class);
    previewNode = previewLoader.getView();
    previewTab.setContent(previewNode);
    preview = previewLoader.getController();
    plainHtml.textProperty().bind(preview.currentHtmlProperty());
  }

  protected void initializePopupPreview() {
    DefaultLoader<Node, AsciiDocViewer> previewLoader = initialization.loadAdditionalController(AsciiDocViewer.class);
    popupPreviewNode = previewLoader.getView();
    popupPreview = previewLoader.getController();
  }

  protected void searchForText() {
    String searchKey = searchField.textProperty().getValueSafe().toLowerCase(Locale.ROOT);
    String editorContent = editor.textProperty().getValueSafe().toLowerCase(Locale.ROOT);
    searchField.setDisable(true);
    CompletableFuture<Integer> search = CompletableFuture.supplyAsync(() -> {
      if (lastSearch != null && lastSearch.matches(searchKey, editorContent)) {
        int startPoint = lastSearch.getPosition() + searchKey.length();
        int newPosition;
        if (startPoint >= editorContent.length()) {
          newPosition = -1;
        } else {
          newPosition = editorContent.substring(startPoint).indexOf(searchKey);
          if (newPosition >= 0) {
            newPosition += lastSearch.getPosition() + searchKey.length();
          }
        }

        if (newPosition == -1) {
          newPosition = editorContent.indexOf(searchKey);
        }
        lastSearch.setPosition(newPosition);
        return newPosition;
      } else {
        int newPosition = editorContent.indexOf(searchKey);
        lastSearch = new LastSearch(searchKey, editorContent).setPosition(newPosition);
        return newPosition;
      }
    }, controller.getExecutorService());

    search.thenAcceptAsync(index -> {
      searchField.setDisable(false);
      if (index >= 0) {
        editor.positionCaret(index);
        editor.requestFocus();
      }
    }, controller.getJavaFXExecutor());
  }

  protected void showSearchField() {
    searchField.setVisible(true);
    searchField.requestFocus();
  }

  protected void storeBack(String storeBack) {
    if (this.persistentStoreBack != null) {
      this.persistentStoreBack.save(storeBack);
    }
  }

  private void addCommands() {
    CDI.current().select(AsciiDocEditorCommand.class).forEach(c -> {
      Button button = new Button();
      button.setMnemonicParsing(true);
      button.focusedProperty().addListener((p, o, n) -> {
        if (n) {
          focusOnEditor = false;
        }
      });
      c.initialize(this, button);
      this.commands.put(c.getClass(), c);
      button.setText(Localized.get(c.getName()));
      button.setOnAction(e -> c.execute(editor));
      editorCommandPane.getChildren().add(button);
    });
  }

  @FXML
  void saveToFile() {
    FileChooser fileChooser = new FileChooser();

    FileChooser.ExtensionFilter htmlFilter = new FileChooser.ExtensionFilter("html", "html");
    FileChooser.ExtensionFilter adocFilter = new FileChooser.ExtensionFilter("adoc", "adoc");

    fileChooser.getExtensionFilters().add(htmlFilter);
    fileChooser.getExtensionFilters().add(adocFilter);
    if (lastFile != null) {
      fileChooser.setInitialDirectory(lastFile.getParentFile());
      fileChooser.setInitialFileName(lastFile.getName());
      if (lastFile.getName().endsWith(".html")) {
        fileChooser.setSelectedExtensionFilter(htmlFilter);
      } else {
        fileChooser.setSelectedExtensionFilter(adocFilter);
      }
    } else {
      fileChooser.setInitialFileName("export.html");
    }

    File file = fileChooser.showSaveDialog(saveToFileButton.getScene().getWindow());
    if (file == null) {
      return;
    }
    this.lastFile = file;
    String extension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);
    if (!file.getName().endsWith(extension)) {
      file = new File(file.getPath() + extension);
    }

    if (extension.endsWith("adoc")) {
      try {
        Files.write(editor.getText(), file, Charsets.UTF_8);
      } catch (IOException e) {
        log.error("Could not write file {}", file, e);
      }
    } else {
      this.parser.renderToFile(editor.getText(), AsciiDocBackend.HTML5, file);
    }
  }

  @FXML
  void showHelp() {
    String title = Localized.get("help");
    title = StringUtils.remove(title, "_");
    helpDialog = new Dialog(this.help, title);

    helpDialog.setContent(helpView);

    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      helpDialog.getStylesheets().add(sheet);
    });

    Stage stage = (Stage) helpDialog.getWindow();
    stage.initModality(Modality.NONE);

    helpDialog.show();
  }

  @FXML
  void showPreviewPopup() {
    if (previewPopupStage == null) {
      String title = Localized.get("adoc.preview");

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

  public void hideActionBar() {
    RowConstraints rowConstraints = mainPane.getRowConstraints().get(1);
    rowConstraints.setMinHeight(0.0);
    rowConstraints.setPrefHeight(0.0);
    rowConstraints.setMaxHeight(0.0);
  }

  public TextArea getEditor() {
    return editor;
  }

  @SuppressWarnings("unchecked")
  public <T extends AsciiDocEditorCommand> T getCommand(Class<T> clazz) {
    return (T) commands.get(clazz);
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

  public AsciiDocEditor setPersistentStoreBack(String id, File dir) {
    this.persistentStoreBack = new PersistentStoreBack(id, dir);
    return this;
  }

  public boolean removePersistentStoreBack() {
    if (persistentStoreBack != null) {
      persistentStoreBack = null;
      return true;
    } else {
      return false;
    }
  }

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void onRefresh(ActivityLoadFinishedEvent e) {
    controller.getJavaFXExecutor().submit(() -> {
      if (editor.textProperty().getValueSafe().trim().isEmpty()) {
        if (persistentStoreBack != null) {
          String text = persistentStoreBack.load();
          editor.setText(text);
        }
      }
    });
  }

  @Override
  public void duringLoad(Object model) {
    //nope
  }

  @Override
  public void duringSave(Object model) {
    if (persistentStoreBack != null) {
      persistentStoreBack.delete();
    }
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

  public PersistentStoreBack getPersistentStoreBack() {
    return persistentStoreBack;
  }
}
