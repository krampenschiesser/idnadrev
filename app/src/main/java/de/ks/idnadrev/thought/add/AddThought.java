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

package de.ks.idnadrev.thought.add;

import de.ks.BaseController;
import de.ks.activity.ModelBound;
import de.ks.executor.group.LastTextChange;
import de.ks.file.FileViewController;
import de.ks.idnadrev.entity.Thought;
import de.ks.text.AsciiDocEditor;
import de.ks.validation.validators.NamedEntityMustNotExistValidator;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
@ModelBound(Thought.class)
public class AddThought extends BaseController<Thought> {
  private static final Logger log = LoggerFactory.getLogger(AddThought.class);

  @FXML
  private GridPane root;
  @FXML
  protected StackPane descriptionContainer;
  protected AsciiDocEditor description;
  @FXML
  protected TextField name;
  @FXML
  protected Button save;
  @FXML
  protected FileViewController fileViewController;
  @FXML
  protected GridPane fileView;
  private LastTextChange lastTextChange;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(descriptionContainer.getChildren()::add, this::setDescription);
    bindValidation();
    fileViewController.getFiles().addListener((ListChangeListener<File>) change -> {
      ObservableList<? extends File> list = change.getList();
      if (list.size() == 1) {
        if (name.textProperty().isEmpty().get() && description.getText().isEmpty()) {
          File file = list.get(0);
          name.setText(file.getName());
          description.setText(file.getAbsolutePath());
          save.requestFocus();
        }
      }
    });

    description.hideActionBar();
    StringProperty descriptionBinding = store.getBinding().getStringProperty(Thought.class, t -> t.getDescription());
    descriptionBinding.bind(description.textProperty());

    fileViewController.setImageData(description.getImages());

    lastTextChange = new LastTextChange(name, controller.getExecutorService());
    lastTextChange.registerHandler(cf -> {
      cf.thenAcceptAsync(name -> {
        String desc = description.textProperty().getValueSafe().trim();
        int newLines = StringUtils.countMatches(desc, "\n");
        if (desc.isEmpty() || (desc.startsWith("= ") && newLines <= 1)) {
          description.setText("= " + name + "\n");
        }
      }, controller.getJavaFXExecutor());
    });
  }

  private void bindValidation() {
    save.disableProperty().bind(validationRegistry.invalidProperty());
    validationRegistry.registerValidator(name, new NamedEntityMustNotExistValidator<>(Thought.class, t -> t.getId() == store.<Thought>getModel().getId()));
  }

  private void setDescription(AsciiDocEditor description) {
    this.description = description;
  }

  @FXML
  void saveThought(ActionEvent e) {
    if (!save.isDisabled()) {
      save.getOnAction().handle(e);
    }
  }

  @FXML
  void onMouseEntered() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    log.trace("Mouse entered {}", clipboard.hasString() ? "Clipboard has string" : "Clipboard has no string");
    processClipboard(clipboard);
  }

  protected void processClipboard(Clipboard clipboard) {
    String text = this.description.getText();
    if (clipboard.hasString() && //
            (text == null || (text != null && text.isEmpty()))) {
      String clipboardString = clipboard.getString();
      int endOfFirstLine = clipboardString.indexOf("\n");
      boolean nameIsEmpty = this.name.textProperty().isEmpty().get();
      if (endOfFirstLine > 0 && nameIsEmpty) {
        this.name.setText(clipboardString.substring(0, endOfFirstLine));
        this.save.requestFocus();
      } else if (nameIsEmpty) {
        this.name.setText(clipboardString);
        this.name.requestFocus();
      }
      this.description.setText(clipboardString);
    }

    if (clipboard.hasFiles()) {
      fileViewController.addFiles(clipboard.getFiles());
    }
  }

  @FXML
  void onDragDrop(DragEvent event) {
    this.save.getScene().getWindow().requestFocus();
    Dragboard dragboard = event.getDragboard();
    if (dragboard.hasFiles()) {
      fileViewController.addFiles(dragboard.getFiles());
    }
  }

  @FXML
  void onDragOver(DragEvent event) {
    Object source = event.getSource();
    Object gestureTarget = event.getGestureTarget();
    log.trace("Drag detected from source {}", source);
    event.acceptTransferModes(TransferMode.ANY);
    event.consume();
  }

  @FXML
  void onSave() {
    controller.save();
    controller.reload();
  }

  @Override
  protected void onRefresh(Thought model) {
    this.name.requestFocus();
    this.description.setText("");
  }

  public AsciiDocEditor getDescription() {
    return description;
  }

  public TextField getName() {
    return name;
  }

  public Button getSave() {
    return save;
  }
}
