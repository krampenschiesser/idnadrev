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
package de.ks.javafx.event;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearTextOnEscape implements EventHandler<KeyEvent> {
  private static final Logger log = LoggerFactory.getLogger(ClearTextOnEscape.class);

  @Override
  public void handle(KeyEvent e) {
    if (e.getCode() == KeyCode.ESCAPE) {
      Object source = e.getSource();
      if (source instanceof TextField) {
        TextField textField = (TextField) source;
        if (!textField.textProperty().getValueSafe().trim().isEmpty()) {
          textField.setText("");
          log.info("###CONSUMING");
          e.consume();
          return;
        }
      }
    }
  }
}
