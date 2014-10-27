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
package de.ks.idnadrev.tag;

import de.ks.BaseController;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.application.fxml.DefaultLoader;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.Tagged;
import de.ks.persistence.PersistentWork;
import de.ks.selection.NamedPersistentObjectSelection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TagContainer extends BaseController<Tagged> {
  @FXML
  protected NamedPersistentObjectSelection<Tag> tagAddController;
  @FXML
  protected StackPane tagSelectionContainer;
  @FXML
  protected FlowPane tagPane;

  protected final ObservableSet<String> currentTags = FXCollections.observableSet(new TreeSet<String>());
  protected boolean readOnly = false;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tagAddController.from(Tag.class);
    tagAddController.setHideOnSingleClick(true);
    tagAddController.setOnAction(e -> {
      TextField input = tagAddController.getInput();
      addTag(input.getText());
      input.clear();
    });
    currentTags.addListener(this::onTagsChanged);
  }

  private void onTagsChanged(SetChangeListener.Change<? extends String> change) {
    String addedTag = change.getElementAdded();
    if (addedTag != null) {
      CompletableFuture.supplyAsync(() -> {
        DefaultLoader<GridPane, TagInfo> loader = new DefaultLoader<>(TagInfo.class);
        loader.load();
        return loader;
      }, controller.getExecutorService()).thenAcceptAsync((loader) -> {
//        currentTags.add(addedTag);
        TagInfo ctrl = loader.getController();
        ctrl.getName().setText(addedTag);
        GridPane view = loader.getView();
        view.setId(addedTag);
        ctrl.getRemove().setOnAction((e) -> {
          currentTags.remove(addedTag);
          tagPane.getChildren().remove(view);
        });
        tagPane.getChildren().add(view);
      }, controller.getJavaFXExecutor());
    }

    String removedTag = change.getElementRemoved();
    if (removedTag != null) {
      Optional<Node> first = tagPane.getChildren().stream().filter(n -> removedTag.equals(n.getId())).findFirst();
      if (first.isPresent()) {
        tagPane.getChildren().remove(first.get());
      }
    }
  }

  public void addTag(String tag) {
    currentTags.add(tag);
  }

  public void removeTag(String tag) {
    currentTags.remove(tag);
  }

  @Override
  public void duringSave(Tagged model) {
    if (!readOnly) {
      tagPane.getChildren().stream().filter(c -> c.getId() != null).map(c -> new Tag(c.getId())).forEach(tag -> {
        Tag readTag = PersistentWork.forName(Tag.class, tag.getName());
        readTag = readTag == null ? tag : readTag;
        model.addTag(readTag);
      });
    }
  }

  @Override
  public void duringLoad(Tagged model) {
    super.duringLoad(model);
    if (!readOnly) {
      model.getTags().forEach(t -> t.getName());
    }
  }

  @Override
  protected void onRefresh(Tagged model) {
    super.onRefresh(model);
    if (!readOnly) {
      currentTags.clear();

      List<String> tags = model.getTags().stream().map(t -> t.getName()).collect(Collectors.toList());
      Collections.sort(tags);
      currentTags.addAll(tags);
    }
  }

  @Override
  protected Tagged extractFromEvent(ActivityLoadFinishedEvent e) {
    if (readOnly) {
      return null;
    } else {
      return super.extractFromEvent(e);
    }
  }

  public TextField getInput() {
    return tagAddController.getInput();
  }

  public EventHandler<ActionEvent> getOnAction() {
    return tagAddController.getOnAction();
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public ObservableSet<String> getCurrentTags() {
    return currentTags;
  }
}
