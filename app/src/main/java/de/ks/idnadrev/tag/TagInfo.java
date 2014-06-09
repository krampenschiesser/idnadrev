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
package de.ks.idnadrev.tag;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TagInfo implements Initializable {
  @FXML
  private Label name;
  @FXML
  private Button remove;
  @FXML
  private GridPane root;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    remove.setVisible(false);
    hideColumn();
    root.setOnMouseEntered((e) -> {
      remove.setVisible(true);
      showColumn();
    });
    root.setOnMouseExited((e) -> {
      remove.setVisible(false);
      hideColumn();
    });
  }

  protected void showColumn() {
    ColumnConstraints column = root.getColumnConstraints().get(1);
    column.setPrefWidth(Control.USE_COMPUTED_SIZE);
    column.setMinWidth(Control.USE_COMPUTED_SIZE);
  }

  protected void hideColumn() {
    ColumnConstraints column = root.getColumnConstraints().get(1);
    column.setPrefWidth(0);
    column.setMinWidth(0);
  }

  public Button getRemove() {
    return remove;
  }

  public Label getName() {
    return name;
  }
}
