/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
 *
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

package de.ks.beagle.thought.collect;


import de.ks.activity.ModelBound;
import de.ks.beagle.entity.Thought;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  protected TextArea description;
  @FXML
  protected TextField name;


  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }


  @FXML
  void onMouseEntered(MouseEvent event) {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    log.info("Mouse entered {}", clipboard.hasString() ? "Clipboard has string" : "Clipboard has no string");
    processClipboard(clipboard);
  }

  protected void processClipboard(Clipboard clipboard) {
    String text = this.description.getText();
    if (clipboard.hasString() && //
            (text == null || (text != null && text.isEmpty()))) {
      String clipboardString = clipboard.getString();
      int endOfFirstLine = clipboardString.indexOf("\n");
      if (endOfFirstLine > 0) {
        this.name.setText(clipboardString.substring(0, endOfFirstLine));
      }
      this.description.setText(clipboardString);
      this.name.requestFocus();
    }
  }

  @FXML
  void onDragDrop(DragEvent event) {
    event.getDragboard().getContentTypes().forEach((t) -> log.info("Got drag type {}", t));
    event.getDragboard().getFiles().forEach((f) -> log.info("Got file from dragtype {}", f));
    log.info("Got html from dragtype {}", event.getDragboard().getHtml());
    log.info("Got imagefrom dragtype {}", event.getDragboard().getImage());
    log.info("Got string from dragtype {}", event.getDragboard().getString());
    log.info("Got url from dragtype {}", event.getDragboard().getUrl());
    log.info("Got rtf from dragtype {}", event.getDragboard().getRtf());
  }

  @FXML
  void onDragOver(DragEvent event) {
    Object source = event.getSource();
    Object gestureTarget = event.getGestureTarget();
    log.trace("Drag detected from source {}", source);
    event.acceptTransferModes(TransferMode.ANY);
    event.consume();
  }
}
