/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.task.view;

import de.ks.idnadrev.adoc.ui.TagSelection;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.idnadrev.repository.ui.RepositorySeletor;
import de.ks.idnadrev.task.TaskState;
import de.ks.standbein.BaseController;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskFilter extends BaseController<Object> {
  private static final Logger log = LoggerFactory.getLogger(TaskFilter.class);
  @FXML
  TextField title;
  @FXML
  ComboBox<String> context;
  @FXML
  RepositorySeletor repositoryController;
  @FXML
  TagSelection tagsController;
  @FXML
  ComboBox<String> state;

  @Inject
  Index index;

  private String wildCard;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    wildCard = localized.get("all");
    state.getItems().add(wildCard);
    state.getItems().addAll(Stream.of(TaskState.values()).filter(s -> s != TaskState.UNPROCESSED).map(TaskState::name).collect(Collectors.toList()));
    state.getSelectionModel().select(0);

    EventStream<Tuple5<Change<String>, Change<String>, Change<String>, Change<String>, ListChangeListener.Change<? extends String>>> combined = EventStreams.combine(//
      EventStreams.changesOf(title.textProperty()).withDefaultEvent(new Change<>(null, null)),//
      EventStreams.changesOf(context.valueProperty()).withDefaultEvent(new Change<>(null, null)),//
      EventStreams.changesOf(state.valueProperty()).withDefaultEvent(new Change<>(null, null)),//
      EventStreams.changesOf(repositoryController.getRepositoryCombo().valueProperty()).withDefaultEvent(new Change<>(null, null)),//
      EventStreams.changesOf(tagsController.getSelectedTags()).withDefaultEvent(null)//
    );
    combined.successionEnds(Duration.ofMillis(300)).subscribe(e -> refreshView());
  }

  void refreshView() {
    String title = this.title.textProperty().getValueSafe().toLowerCase(Locale.ROOT).trim();
    String context = Optional.ofNullable(this.context.valueProperty().getValue()).orElse("").toLowerCase(Locale.ROOT).trim();
    List<String> selectedTags = new ArrayList<>(tagsController.getSelectedTags());
    String repository = Optional.ofNullable(this.repositoryController.getRepositoryCombo().getValue()).orElse("").toLowerCase(Locale.ROOT).trim();
    String state = Optional.ofNullable(this.state.getValue()).orElse("").toLowerCase(Locale.ROOT).trim();

    store.getDatasource().setLoadingHint(new ViewTasksDs.TaskDsFilter(title, context, state, selectedTags, repository, wildCard.toLowerCase(Locale.ROOT)));
    store.reload();
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    List<String> contexts = index.queryValues(StandardQueries.contextQuery(), s -> true).stream().map(s -> s.toLowerCase(Locale.ROOT)).collect(Collectors.toList());
    Collections.sort(contexts);
    ObservableList<String> items = context.getItems();
    items.clear();
    items.addAll(wildCard);
    items.addAll(contexts);
    context.getSelectionModel().select(0);
  }

}
