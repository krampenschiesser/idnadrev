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

package de.ks.idnadrev.thought.collect;

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.ModelBound;
import de.ks.activity.context.ActivityStore;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.thought.collect.file.FileThoughtViewController;
import de.ks.text.AsciiDocEditor;
import de.ks.validation.ValidationRegistry;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
@ModelBound(Thought.class)
public class AddThought implements Initializable {
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
  protected FileThoughtViewController fileViewController;
  @FXML
  protected GridPane fileView;
  @Inject
  ValidationRegistry validationRegistry;
  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;

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
  }

  private void bindValidation() {
    save.disableProperty().bind(validationRegistry.getValidationSupport().invalidProperty());
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
    boolean clearClipboard = false;
    String text = this.description.getText();
    if (clipboard.hasString() && //
            (text == null || (text != null && text.isEmpty()))) {
      String clipboardString = clipboard.getString();
      int endOfFirstLine = clipboardString.indexOf("\n");
      if (endOfFirstLine > 0 && this.name.textProperty().isEmpty().get()) {
        this.name.setText(clipboardString.substring(0, endOfFirstLine));
        this.save.requestFocus();
      } else {
        this.name.requestFocus();
      }
      this.description.setText(clipboardString);
      clearClipboard = true;
    }

    if (clipboard.hasFiles()) {
      fileViewController.addFiles(clipboard.getFiles());
      clearClipboard = true;
    }
    if (clearClipboard) {
      clipboard.clear();
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

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void onRefresh(ActivityLoadFinishedEvent event) {
    this.name.requestFocus();
    this.description.setText("");
  }
}
