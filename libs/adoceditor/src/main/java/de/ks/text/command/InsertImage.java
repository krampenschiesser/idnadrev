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
package de.ks.text.command;

import de.ks.standbein.activity.initialization.ActivityInitialization;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.javafx.FxCss;
import de.ks.text.AsciiDocEditor;
import de.ks.text.image.ImageData;
import de.ks.text.image.ImageProvider;
import de.ks.text.image.SelectImageController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class InsertImage implements AsciiDocEditorCommand {
  @Inject
  ActivityInitialization initialization;
  @Inject
  Set<ImageProvider> imageProviders;
  @Inject
  @FxCss
  Set<String> cssSheets;
  @Inject
  Localized localized;

  protected final ObservableSet<ImageData> images = FXCollections.observableSet(new TreeSet<ImageData>(Comparator.comparing(id -> id.getName())));
  protected SelectImageController selectImageController;
  protected Stage dialog;
  protected Button button;

  @Override
  public void initialize(AsciiDocEditor editor, Button button) {
    this.button = button;

    collectImages();
    selectImageController = initialization.loadAdditionalController(SelectImageController.class).getController();

    images.addListener(this::imagesModified);
    selectImageController.selectedImagePathProperty().addListener((p, o, n) -> {
      if (n != null) {
        if (dialog != null) {
          dialog.hide();
        }

        insert(editor.getEditor(), n);
      }
    });
  }

  public void collectImages() {
    imageProviders.forEach(p -> images.addAll(p.getImages()));
  }

  @Override
  public String getInsertText() {
    return null;
  }

  @Override
  public void execute(TextArea editor) {
    collectImages();
    if (dialog == null) {
      dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(button.getScene().getWindow());
      dialog.setTitle(localized.get("select.image"));
    }

    StackPane container = new StackPane();
    container.setPadding(new Insets(5));
    container.getChildren().add(selectImageController.getImagePane());
    Scene scene = new Scene(container);
    dialog.setOnHiding(e -> {
      scene.setRoot(new StackPane());
    });
    dialog.setScene(scene);

    cssSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });
    dialog.show();
  }

  public void insert(TextArea editor, String imagePath) {
    String insertText = "\nimage::file:///" + imagePath + "[]\n";
    int insertPosition = editor.getCaretPosition();
    editor.insertText(insertPosition, insertText);
    editor.positionCaret(insertPosition + insertText.length() - 2);
    editor.requestFocus();
  }

  public void imagesModified(SetChangeListener.Change<? extends ImageData> change) {
    if (change.wasAdded()) {
      ImageData elementAdded = change.getElementAdded();
      selectImageController.addImage(elementAdded.getName(), elementAdded.getPath());
    }
    if (change.wasRemoved()) {
      ImageData elementRemoved = change.getElementRemoved();
      selectImageController.removeImage(elementRemoved.getName());
    }
  }

  public SelectImageController getSelectImageController() {
    return selectImageController;
  }
}
