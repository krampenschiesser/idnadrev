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
package de.ks.idnadrev.thought.view;

import de.ks.activity.ActivityController;
import de.ks.activity.ListBound;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

@ListBound(Thought.class)
public class ViewThoughts implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(ViewThoughts.class);

  @Inject
  ActivityController controller;
  @FXML
  TableView<Thought> _this;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    @SuppressWarnings("unchecked") TableColumn<Thought, String> nameColumn = (TableColumn<Thought, String>) _this.getColumns().get(0);
    @SuppressWarnings("unchecked") TableColumn<Thought, String> descriptionColumn = (TableColumn<Thought, String>) _this.getColumns().get(1);

    DoubleBinding width25 = _this.widthProperty().multiply(.25D);
    nameColumn.prefWidthProperty().bind(width25);
    DoubleBinding width75 = _this.widthProperty().multiply(.75D);
    descriptionColumn.prefWidthProperty().bind(width75);

    _this.setRowFactory((view) -> {
      TableRow<Thought> thoughtTableRow = new TableRow<Thought>();
      thoughtTableRow.setMaxHeight(25);
      thoughtTableRow.setPrefHeight(25);
      return thoughtTableRow;
    });
  }

  public void postPone() {
    Thought selectedItem = _this.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {

      PersistentWork.runAsync((em) -> em.find(Thought.class, selectedItem.getId()).postPone())//
              .thenRun(() -> {
                log.info("Postponing {}", selectedItem);
                controller.reload();
              });
    }
  }

  public Thought getSelectedThought() {
    return _this.getSelectionModel().getSelectedItem();
  }
}
