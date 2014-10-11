/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.information.view.preview;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.information.text.TextInfoActivity;
import de.ks.idnadrev.information.view.InformationPreviewItem;
import de.ks.persistence.PersistentWork;
import de.ks.text.view.AsciiDocContent;
import de.ks.text.view.AsciiDocViewer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TextInfoPreview extends BaseController<List<InformationPreviewItem>> implements InformationPreview<TextInfo> {

  @FXML
  protected GridPane root;
  @FXML
  protected Button edit;
  @FXML
  protected StackPane adocContainer;

  protected AsciiDocViewer asciiDocViewer;
  protected final Map<String, TextInfo> infos = new ConcurrentHashMap<>();
  protected volatile InformationPreviewItem selectedItem;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocViewer.load(root -> adocContainer.getChildren().add(root), viewer -> asciiDocViewer = viewer);
  }

  @Override
  protected void onRefresh(List<InformationPreviewItem> model) {
    asciiDocViewer.clear();
//    infos.keySet().retainAll(model.stream().map(InformationPreviewItem::getName).collect(Collectors.toSet()));
    infos.clear();
    model.stream()//
      .filter(preview -> preview.getType().equals(TextInfo.class))//
      .map(this::load)//
      .forEach(cf -> cf.thenAccept(this::processLoadedTextInfo));
  }

  protected void processLoadedTextInfo(TextInfo item) {
    if (item != null) {
      AsciiDocContent content = new AsciiDocContent(item.getName(), item.getDescription());
      infos.put(item.getName(), item);
      if (selectedItem != null && item.getName().equals(selectedItem.getName())) {
        controller.getJavaFXExecutor().submit(() -> asciiDocViewer.show(content));
      }
      asciiDocViewer.preload(Collections.singleton(content));
    }
  }

  protected CompletableFuture<TextInfo> load(InformationPreviewItem preview) {
    return CompletableFuture.supplyAsync(() -> PersistentWork.forName(TextInfo.class, preview.getName()), controller.getExecutorService());
  }

  @FXML
  void onEdit() {
    ActivityHint activityHint = new ActivityHint(TextInfoActivity.class, controller.getCurrentActivityId());
    String name = selectedItem.getName();
    TextInfo textInfo = infos.get(name);
    infos.remove(name);

    activityHint.setDataSourceHint(() -> textInfo);
    controller.startOrResume(activityHint);
  }

  public GridPane show(InformationPreviewItem item) {
    this.selectedItem = item;
    TextInfo textInfo = infos.get(item.getName());
    if (textInfo != null) {
      AsciiDocContent content = new AsciiDocContent(textInfo.getName(), textInfo.getDescription());
      asciiDocViewer.show(content);
    }
    return root;
  }

  @Override
  public void edit() {
    onEdit();
  }
}
