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

import de.ks.activity.initialization.ActivityInitialization;
import de.ks.i18n.Localized;
import de.ks.javafx.FxCss;
import de.ks.text.AsciiDocEditor;
import de.ks.text.image.ImageData;
import de.ks.text.image.ImageProvider;
import de.ks.text.image.SelectImageController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.controlsfx.dialog.Dialog;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.TreeSet;

public class InsertImage implements AsciiDocEditorCommand {
  @Inject
  ActivityInitialization initialization;
  @Inject
  Instance<ImageProvider> imageProviders;

  protected final ObservableSet<ImageData> images = FXCollections.observableSet(new TreeSet<ImageData>(Comparator.comparing(id -> id.getName())));
  protected SelectImageController selectImageController;
  private Dialog dialog;
  private Button button;

  @Override
  public void initialize(AsciiDocEditor editor, Button button) {
    this.button = button;

    collectImages();
    selectImageController = initialization.loadAdditionalController(SelectImageController.class).getController();

    images.addListener(this::imagesModified);
    selectImageController.selectedImagePathProperty().addListener((p, o, n) -> {
      if (dialog != null) {
        dialog.hide();
      }

      insert(editor.getEditor(), n);
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
    dialog = new Dialog(button, Localized.get("select.image"));
    dialog.setContent(selectImageController.getImagePane());
    Instance<String> styleSheets = CDI.current().select(String.class, FxCss.LITERAL);
    styleSheets.forEach((sheet) -> {
      dialog.getStylesheets().add(sheet);
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
