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
package de.ks.idnadrev.adoc.view;

import de.ks.idnadrev.adoc.ui.TagSelection;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.repository.ui.RepositorySeletor;
import de.ks.standbein.BaseController;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.reactfx.Change;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Tuple3;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.util.*;

public class AdocFilter extends BaseController<Object> {
  @FXML
  TextField title;
  @FXML
  RepositorySeletor repositoryController;
  @FXML
  TagSelection tagsController;

  @Inject
  Index index;

  private String wildCard;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    wildCard = localized.get("all");
    EventStream<Tuple3<Change<String>, Change<String>, ListChangeListener.Change<? extends String>>> combined = EventStreams.combine(//
      EventStreams.changesOf(title.textProperty()).withDefaultEvent(new Change<>(null, null)),//
      EventStreams.changesOf(repositoryController.getRepositoryCombo().valueProperty()).withDefaultEvent(new Change<>(null, null)),//
      EventStreams.changesOf(tagsController.getSelectedTags()).withDefaultEvent(null)//
    );
    combined.successionEnds(Duration.ofMillis(300)).subscribe(e -> refreshView());
  }

  void refreshView() {
    String title = this.title.textProperty().getValueSafe().toLowerCase(Locale.ROOT).trim();
    List<String> selectedTags = new ArrayList<>(tagsController.getSelectedTags());
    String repository = Optional.ofNullable(this.repositoryController.getRepositoryCombo().getValue()).orElse("").toLowerCase(Locale.ROOT).trim();

    store.getDatasource().setLoadingHint(new ViewAdocDs.ViewAdocFilter(title, selectedTags, repository, wildCard.toLowerCase(Locale.ROOT)));
    store.reload();
  }
}
