/*
 * Copyright [2015] [Christian Loehnert]
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

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.TaggedEntity;
import de.ks.standbein.table.TableConfigurator;
import de.ks.standbein.table.selection.TextFieldTableSelection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class TagSelection {
  final TextFieldTableSelection<Tag> tagSelection;
  final TableConfigurator<Tag> tagTableConfigurator;
  final PersistentWork persistentWork;

  @Inject
  public TagSelection(TextFieldTableSelection<Tag> tagSelection, TableConfigurator<Tag> tagTableConfigurator, PersistentWork persistentWork) {
    this.tagSelection = tagSelection;
    this.tagTableConfigurator = tagTableConfigurator;
    this.persistentWork = persistentWork;

    TableView<Tag> tagTable = new TableView<>();
    tagTableConfigurator.addText(Tag.class, Tag::getDisplayName);
    tagTableConfigurator.configureTable(tagTable);

    StringConverter<Tag> converter = new StringConverter<Tag>() {
      @Override
      public String toString(Tag object) {
        return object.getDisplayName();
      }

      @Override
      public Tag fromString(String string) {
        return new Tag(string);
      }
    };
    tagSelection.configure(tagTable, this::getTagNames, this::getTags, converter);
  }

  public TextField getTextField() {
    return tagSelection.getTextField();
  }

  public GridPane getRoot() {
    return tagSelection.getRoot();
  }

  public Button getBrowse() {
    return tagSelection.getBrowse();
  }

  public EventHandler<ActionEvent> getOnAction() {
    return tagSelection.getOnAction();
  }

  public void setOnAction(EventHandler<ActionEvent> onAction) {
    tagSelection.setOnAction(onAction);
  }

  private List<Tag> getTags(String s) {
    Set<Set<Tag>> sets = persistentWork.queryValues(TaggedEntity.class, TaggedEntity.byTags(), l -> true);
    Set<Tag> collect = sets.stream().flatMap(Collection::stream).collect(Collectors.toSet());
    ArrayList<Tag> tags = new ArrayList<>(collect);
    Collections.sort(tags);
    return tags;//fixme performance
  }

  private List<String> getTagNames(String s) {
    Set<Set<Tag>> sets = persistentWork.queryValues(TaggedEntity.class, TaggedEntity.byTags(), l -> true);
    Set<String> collect = sets.stream().flatMap(Collection::stream).map(Tag::getDisplayName).collect(Collectors.toSet());
    ArrayList<String> tags = new ArrayList<>(collect);
    Collections.sort(tags);
    return tags;//fixme performance
  }
}
