/**
 * Copyright [2014] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.information.view.preview;

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.information.text.TextInfoActivity;
import de.ks.standbein.BaseController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.texteditor.preview.TextPreview;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class TextInfoPreview extends BaseController<List<Information>> {
  @FXML
  protected StackPane adocContainer;
  @Inject
  PersistentWork persistentWork;

  protected TextPreview asciiDocViewer;
  protected final Map<String, Information> infos = new ConcurrentHashMap<>();
  protected volatile Information selectedItem;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextPreview.load(activityInitialization, root -> adocContainer.getChildren().add(root), viewer -> asciiDocViewer = viewer);
  }

  @Override
  protected void onRefresh(List<Information> model) {
    asciiDocViewer.clear();
    infos.clear();
    model.stream().forEach(this::processTextInfo);
  }

  protected void processTextInfo(Information item) {
    if (item != null) {
      infos.put(item.getName(), item);
      AdocFile adocFile = item.getAdocFile();
      if (adocFile != null) {
        if (selectedItem != null && item.getName().equals(selectedItem.getName())) {
          controller.getJavaFXExecutor().submit(() -> asciiDocViewer.show(adocFile.getTmpFile()));
        }
        asciiDocViewer.preload(adocFile.getTmpFile(), adocFile.getRenderingPath(), adocFile.getContent());
      }
    }
  }

  public Pane show(Information item) {
    if (item == null) {
      asciiDocViewer.clearContent();
    } else {
      this.selectedItem = item;
      Information textInfo = infos.get(item.getName());
      if (textInfo != null) {
        AdocFile adocFile = textInfo.getAdocFile();
        if (adocFile != null) {
          asciiDocViewer.show(adocFile.getTmpFile());
        } else {
          asciiDocViewer.clearContent();
        }
      }
    }
    return adocContainer;
  }

  public void edit() {
    ActivityHint activityHint = new ActivityHint(TextInfoActivity.class, controller.getCurrentActivityId());
    String name = selectedItem.getName();
    Information textInfo = infos.get(name);
    infos.remove(name);

    activityHint.setDataSourceHint(() -> textInfo);
    controller.startOrResume(activityHint);
  }

  public Information getCurrentItem() {
    String name = selectedItem.getName();
    return infos.get(name);
  }
}
