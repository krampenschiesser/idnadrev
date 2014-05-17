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
package de.ks.beagle.thought.view;

import de.ks.activity.ActivityController;
import de.ks.activity.ListBound;
import de.ks.beagle.entity.Thought;
import de.ks.persistence.PersistentWork;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@ListBound(Thought.class)
public class ViewThoughts {
  private static final Logger log = LoggerFactory.getLogger(ViewThoughts.class);

  @Inject
  ActivityController controller;
  @FXML
  TableView<Thought> _this;

  public void postPone(ActionEvent event) {
    Thought selectedItem = _this.getSelectionModel().getSelectedItem();
    PersistentWork.run((em) -> em.find(Thought.class, selectedItem.getId()).postPone());
    log.info("Postponing {}", selectedItem);
    controller.reload();
  }
}
