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

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.idnadrev.information.text.TextInfoActivity;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.idnadrev.thought.add.AddThoughtActivity;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.texteditor.preview.TextPreview;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

public class ViewThoughts extends BaseController<List<Thought>> {
  private static final Logger log = LoggerFactory.getLogger(ViewThoughts.class);
  @Inject
  ActivityExecutor executor;
  @Inject
  PersistentWork persistentWork;

  @FXML
  protected TableView<Thought> thoughtTable;
  @FXML
  protected TableColumn<Thought, String> nameColumn;
  @FXML
  protected Label nameLabel;
  @FXML
  protected StackPane description;
  @FXML
  protected Button toTask;
  @FXML
  protected Button toTextInfo;
  @FXML
  protected Button later;
  @FXML
  protected Button deleteBtn;
  @FXML
  protected Button editBtn;

  protected volatile TextPreview asciiDocViewer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextPreview.load(activityInitialization, view -> description.getChildren().add(view), ctrl -> asciiDocViewer = ctrl);

    thoughtTable.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n == null) {
        asciiDocViewer.clear();
      } else {
        asciiDocViewer.show(n.getAdocFile().getTmpFile());
      }
    });
    @SuppressWarnings("unchecked")
    TableColumn<Thought, String> nameColumn = (TableColumn<Thought, String>) thoughtTable.getColumns().get(0);

    DoubleBinding width100 = thoughtTable.widthProperty().multiply(1D);
    nameColumn.prefWidthProperty().bind(width100);

    thoughtTable.setRowFactory((view) -> {
      TableRow<Thought> thoughtTableRow = new TableRow<Thought>();
      thoughtTableRow.setMaxHeight(25);
      thoughtTableRow.setPrefHeight(25);
      return thoughtTableRow;
    });
    this.nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
    thoughtTable.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      updateSelection(n);
    });

    BooleanBinding disable = thoughtTable.getSelectionModel().selectedItemProperty().isNull();
    toTask.disableProperty().bind(disable);
    toTextInfo.disableProperty().bind(disable);
    later.disableProperty().bind(disable);
    deleteBtn.disableProperty().bind(disable);
    editBtn.disableProperty().bind(disable);
  }

  public void postPone() {
    Thought selectedItem = thoughtTable.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {

      executor.submit(() -> persistentWork.run(session -> {
        Thought thought = session.findById(selectedItem.getId());
        thought.postPone(1);//FIXME use option definition here
        log.info("Postponing {}", selectedItem);
        executor.submit(() -> controller.reload());
      }));
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
    persistentWork.run(em -> {
      Thought thought = thoughtTable.getSelectionModel().getSelectedItem();
      em.remove(persistentWork.reload(thought));
    });
    controller.reload();
  }

  @FXML
  void onTransformToTextInfo() {
    Thought thought = getSelectedThought();

    ActivityHint activityHint = new ActivityHint(TextInfoActivity.class);
    activityHint.setReturnToActivity(controller.getCurrentActivityId());
    activityHint.setDataSourceHint(() -> thought);

    controller.startOrResume(activityHint);
  }

  @FXML
  void onEdit() {
    ActivityHint activityHint = new ActivityHint(AddThoughtActivity.class);
    activityHint.setReturnToActivity(controller.getCurrentActivityId());
    activityHint.setDataSourceHint(this::getSelectedThought);

    controller.startOrResume(activityHint);
  }

  @Override
  protected void onRefresh(List<Thought> thoughts) {
    thoughtTable.setItems(FXCollections.observableList(thoughts));
    thoughts.forEach(t -> {
      AdocFile adocFile = t.getAdocFile();
      if (adocFile == null) {
        asciiDocViewer.clearContent();
      } else {
        asciiDocViewer.preload(adocFile.getTmpFile(), adocFile.getRenderingPath(), adocFile.getContent());
      }
    });
    thoughtTable.requestFocus();
    controller.getJavaFXExecutor().submit(() -> thoughtTable.getSelectionModel().select(0));
  }

  @Override
  public void duringLoad(List<Thought> model) {
//    model.forEach(t->t.getAdocFile().getName());
  }
}
