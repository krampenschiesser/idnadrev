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

import de.ks.activity.ActivityController;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.initialization.LoadInFXThread;
import de.ks.application.fxml.DefaultLoader;
import de.ks.i18n.Localized;
import de.ks.javafx.FxCss;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.Dialog;
import org.fxmisc.richtext.StyleClassedTextArea;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.File;
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

  @Inject
  @FxCss
  Instance<String> stylesheets;
  @Inject
  AsciiDocParser parser;
  @Inject
  ActivityController controller;
  @FXML
  private StyleClassedTextArea editor;
  @FXML
  protected WebView preview;
  @FXML
  protected Button help;
  @FXML
  protected Button saveToFileButton;
  @FXML
  protected GridPane mainPane;

  protected Dialog helpDialog;
  protected WebView helpView;
  protected final SimpleStringProperty text = new SimpleStringProperty();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    helpView = new WebView();
    helpView.getEngine().load("http://powerman.name/doc/asciidoc");

    text.bind(editor.textProperty());
  }

  @FXML
  void insertMath() {

  }

  @FXML
  void insertImage() {

  }

  @FXML
  void saveToFile() {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(saveToFileButton.getScene().getWindow());

  }

  @FXML
  void showHelp() {
    helpDialog = new Dialog(this.help, Localized.get("help"));
    helpDialog.setContent(helpView);
    helpDialog.show();
    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      helpDialog.getStylesheets().add(sheet);
    });

  }

  public ObservableValue<String> textProperty() {
    return text;
  }

  public String getText() {
    return text.get();
  }

  public void setText(String text) {
    this.editor.replaceText(text);
  }

  public void hideActionBar() {
    RowConstraints rowConstraints = mainPane.getRowConstraints().get(1);
    rowConstraints.setMinHeight(0.0);
    rowConstraints.setPrefHeight(0.0);
    rowConstraints.setMaxHeight(0.0);
  }
}
