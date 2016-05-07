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

package de.ks.idnadrev.adoc.ui;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.standbein.BaseController;
import de.ks.texteditor.preview.TextPreview;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.reactfx.EventStreams;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class AdocPreview extends BaseController<List<AdocFile>> {
  @FXML
  protected StackPane root;

  protected final SimpleObjectProperty<AdocFile> selectedTask = new SimpleObjectProperty<>();
  private TextPreview textPreview;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextPreview.load(activityInitialization, pane -> root.getChildren().add(pane), p -> this.textPreview = p);

    EventStreams.valuesOf(selectedTask).filter(Objects::nonNull).subscribe(f -> textPreview.show(f.getPath()));
    EventStreams.valuesOf(selectedTask).filter(Objects::isNull).subscribe(f -> textPreview.clearContent());
  }

  public TextPreview getTextPreview() {
    return textPreview;
  }

  public AdocFile getSelectedTask() {
    return selectedTask.get();
  }

  public SimpleObjectProperty<AdocFile> selectedTaskProperty() {
    return selectedTask;
  }

  public AdocPreview setSelectedTask(AdocFile selectedTask) {
    this.selectedTask.set(selectedTask);
    return this;
  }

  @Override
  protected void onRefresh(List<AdocFile> model) {
    textPreview.clear();
    for (AdocFile adocFile : model) {
      Path parent = adocFile.getPath().getParent();

      String fileName = adocFile.getPath().getFileName().toString();
      fileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".html";
      Path renderTarget = parent.resolve(fileName);
      textPreview.preload(adocFile.getPath(), renderTarget);
    }
  }
}
