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
import com.google.common.io.Files;
import de.ks.activity.ActivityController;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.initialization.LoadInFXThread;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.i18n.Localized;
import de.ks.javafx.FxCss;
import de.ks.text.command.AsciiDocEditorCommand;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@LoadInFXThread
public class AsciiDocEditor implements Initializable {
  public static CompletableFuture<DefaultLoader<Node, AsciiDocEditor>> load(Consumer<StackPane> viewConsumer, Consumer<AsciiDocEditor> controllerConsumer) {
    ActivityInitialization initialization = CDI.current().select(ActivityInitialization.class).get();
    return initialization.loadAdditionalController(AsciiDocEditor.class)//
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
  @FXML
  protected TextArea editor;
  @FXML
  protected WebView preview;
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

  protected Dialog helpDialog;
  protected WebView helpView;
  protected final SimpleStringProperty text = new SimpleStringProperty();
  protected LastExecutionGroup<String> renderGroup;
  protected String previewHtmlString;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    renderGroup = new LastExecutionGroup<>(500, controller.getCurrentExecutorService());
    helpView = new WebView();
    helpView.getEngine().load("http://powerman.name/doc/asciidoc");

    text.bind(editor.textProperty());

    editor.textProperty().addListener((p, o, n) -> {
      renderGroup.schedule(() -> parser.parse(n)).thenAcceptAsync(html -> previewHtmlString = html, controller.getJavaFXExecutor());
    });

    tabPane.getSelectionModel().selectedIndexProperty().addListener((p, o, n) -> {
      if (n != null && n.intValue() == 1) {
        preview.getEngine().loadContent(previewHtmlString);
      }
      if (o == null || n == null) {
        return;
      }
      if (o.intValue() == 1 && n.intValue() == 0) {
        Platform.runLater(() -> editor.requestFocus());
      }
    });

    addCommands();
  }

  private void addCommands() {
    CDI.current().select(AsciiDocEditorCommand.class).forEach(c -> {
      Button button = new Button();
      button.setText(Localized.get(c.getName()));
      button.setOnAction(e -> c.execute(editor));
      editorCommandPane.getChildren().add(button);
    });
  }

  @FXML
  void saveToFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("html", "html"));
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("adoc", "adoc"));
    fileChooser.setInitialFileName("export.html");

    File file = fileChooser.showSaveDialog(saveToFileButton.getScene().getWindow());
    if (file == null) {
      return;
    }
    String extension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);

    String content;
    if (extension.endsWith("adoc")) {
      content = editor.getText();
    } else {
      content = previewHtmlString;
    }
    if (!file.getName().endsWith(extension)) {
      file = new File(file.getPath() + extension);
    }
    log.info("Saving to file {}", file);

    try {
      Files.write(content, file, Charsets.UTF_8);
    } catch (IOException e) {
      log.error("Could not write file {}", file, e);
    }
  }

  @FXML
  void showHelp() {
    helpDialog = new Dialog(this.help, Localized.get("help"));

    helpDialog.setContent(helpView);

    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      helpDialog.getStylesheets().add(sheet);
    });

    Stage stage = (Stage) helpDialog.getWindow();
    stage.initModality(Modality.NONE);

    helpDialog.show();
  }

  public ObservableValue<String> textProperty() {
    return text;
  }

  public String getText() {
    return text.get();
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
}
