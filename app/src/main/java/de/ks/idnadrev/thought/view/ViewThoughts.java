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
package de.ks.idnadrev.thought.view;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.file.FileStore;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.persistence.PersistentWork;
import de.ks.text.view.AsciiDocContent;
import de.ks.text.view.AsciiDocViewer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ViewThoughts extends BaseController<List<Thought>> {
  private static final Logger log = LoggerFactory.getLogger(ViewThoughts.class);
  @Inject
  FileStore fileStore;

  @FXML
  TableView<Thought> thoughtTable;
  @FXML
  private Label nameLabel;
  @FXML
  private StackPane description;
  @FXML
  private Button toTask;
  @FXML
  private Button toInfo;
  @FXML
  private Button later;
  @FXML
  private Button deleteBtn;

  private AsciiDocViewer asciiDocViewer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    activityInitialization.loadAdditionalController(AsciiDocViewer.class).thenAcceptAsync(l -> {
      asciiDocViewer = l.getController();
      asciiDocViewer.addPreProcessor(fileStore::replaceFileStoreDir);
      thoughtTable.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
        if (n == null) {
          asciiDocViewer.reset();
        } else {
          asciiDocViewer.show(new AsciiDocContent(n.getName(), n.getDescription()));
        }
      });
      description.getChildren().add(l.getView());
    }, controller.getJavaFXExecutor());

    @SuppressWarnings("unchecked") TableColumn<Thought, String> nameColumn = (TableColumn<Thought, String>) thoughtTable.getColumns().get(0);

    DoubleBinding width100 = thoughtTable.widthProperty().multiply(1D);
    nameColumn.prefWidthProperty().bind(width100);

    thoughtTable.setRowFactory((view) -> {
      TableRow<Thought> thoughtTableRow = new TableRow<Thought>();
      thoughtTableRow.setMaxHeight(25);
      thoughtTableRow.setPrefHeight(25);
      return thoughtTableRow;
    });

    thoughtTable.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      updateSelection(n);
    });

    BooleanBinding disable = thoughtTable.getSelectionModel().selectedItemProperty().isNull();
    toTask.disableProperty().bind(disable);
    toInfo.disableProperty().bind(disable);
    later.disableProperty().bind(disable);
    deleteBtn.disableProperty().bind(disable);
  }

  public void postPone() {
    Thought selectedItem = thoughtTable.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {

      PersistentWork.runAsync((em) -> em.find(Thought.class, selectedItem.getId()).postPone())//
              .thenRun(() -> {
                log.info("Postponing {}", selectedItem);
                controller.reload();
              });
    }
  }

  public Thought getSelectedThought() {
    Thought selectedItem = thoughtTable.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      TablePosition focusedCell = this.thoughtTable.getFocusModel().getFocusedCell();
      selectedItem = thoughtTable.getFocusModel().getFocusedItem();
    }
    return selectedItem;
  }

  private void updateSelection(Thought thought) {
    if (thought == null) {
      nameLabel.setText(null);
    } else {
      nameLabel.setText(thought.getName());
    }
  }

  @FXML
  void handleKeyReleased(KeyEvent e) {
    if (e.getCode() == KeyCode.ENTER) {
      if (!toTask.isDisabled()) {
        toTask.getOnAction().handle(null);
      }
      e.consume();
    } else if (e.getCode() == KeyCode.DELETE) {
      if (!deleteBtn.isDisabled()) {
        delete();
      }
      e.consume();
    }
  }

  @FXML
  void convertToTask() {
    ActivityHint activityHint = new ActivityHint(CreateTaskActivity.class);
    activityHint.setReturnToActivity(controller.getCurrentActivityId());
    activityHint.setDataSourceHint(this::getSelectedThought);

    controller.startOrResume(activityHint);
  }

  @FXML
  void delete() {
    PersistentWork.run(em -> {
      Thought thought = thoughtTable.getSelectionModel().getSelectedItem();
      em.remove(PersistentWork.reload(thought));
    });
    controller.reload();
  }

  @Override
  protected void onRefresh(List<Thought> thoughts) {
    List<AsciiDocContent> asciiDocContents = thoughts.stream().map(t -> new AsciiDocContent(t.getName(), t.getDescription())).collect(Collectors.toList());
    this.asciiDocViewer.preload(asciiDocContents);
    thoughtTable.requestFocus();
    controller.getJavaFXExecutor().submit(() -> thoughtTable.getSelectionModel().select(0));
  }

  public TableView<Thought> getTable() {
    return thoughtTable;
  }

  public Button getToTask() {
    return toTask;
  }
}
