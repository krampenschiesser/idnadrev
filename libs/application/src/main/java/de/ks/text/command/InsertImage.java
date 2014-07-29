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
import de.ks.text.ImageData;
import de.ks.text.SelectImageController;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.controlsfx.dialog.Dialog;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class InsertImage implements AsciiDocEditorCommand {
  @Inject
  ActivityInitialization initialization;

  protected ObservableList<ImageData> images;
  protected SelectImageController selectImageController;
  private Dialog dialog;
  private Button button;

  @Override
  public void initialize(AsciiDocEditor edior, Button button) {
    this.button = button;
    images = edior.getImages();
    try {
      selectImageController = initialization.loadAdditionalController(SelectImageController.class).get().getController();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    images.addListener(this::imagesModified);
    selectImageController.selectedImagePathProperty().addListener((p, o, n) -> {
      if (dialog != null) {
        dialog.hide();
      }

      insert(edior.getEditor(), n);
    });
  }

  @Override
  public String getInsertText() {
    return null;
  }

  @Override
  public void execute(TextArea editor) {
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

  public void imagesModified(ListChangeListener.Change<? extends ImageData> change) {
    while (change.next()) {
      List<? extends ImageData> addedSubList = change.getAddedSubList();
      List<? extends ImageData> removed = change.getRemoved();
      addedSubList.forEach(i -> selectImageController.addImage(i.getName(), i.getPath()));
      removed.forEach(i -> selectImageController.removeImage(i.getName()));
    }
  }

  public SelectImageController getSelectImageController() {
    return selectImageController;
  }
}
