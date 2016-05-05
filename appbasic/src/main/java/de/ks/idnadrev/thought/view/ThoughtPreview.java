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
package de.ks.idnadrev.thought.view;

import de.ks.idnadrev.task.Task;
import de.ks.standbein.BaseController;
import de.ks.texteditor.preview.TextPreview;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class ThoughtPreview extends BaseController<List<Task>> {
  @FXML
  protected StackPane root;

  protected final SimpleObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
  private TextPreview textPreview;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextPreview.load(activityInitialization, pane -> root.getChildren().add(pane), p -> this.textPreview = p);
    selectedTask.addListener((p, o, n) -> {
      if (n != null) {
        textPreview.show(n.getPath());
      } else {
        textPreview.clearContent();
      }
    });
  }

  public TextPreview getTextPreview() {
    return textPreview;
  }

  @Override
  protected void onRefresh(List<Task> model) {
    for (Task task : model) {
      Path parent = task.getPath().getParent();

      String fileName = task.getPath().getFileName().toString();
      fileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".html";
      Path renderTarget = parent.resolve(fileName);
      textPreview.preload(task.getPath(), renderTarget);
    }
  }
}
