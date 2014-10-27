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
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.idnadrev.information.uml.UmlDiagramActivity;
import de.ks.idnadrev.information.uml.UmlDiagramRender;
import de.ks.idnadrev.information.view.InformationPreviewItem;
import de.ks.imagecache.Images;
import de.ks.persistence.PersistentWork;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UmlPreview extends BaseController<List<InformationPreviewItem>> implements InformationPreview<UmlDiagramInfo> {
  private static final Logger log = LoggerFactory.getLogger(UmlPreview.class);
  @FXML
  protected ImageView imageView;
  @FXML
  protected StackPane imageContainer;
  @FXML
  protected GridPane root;
  @FXML
  protected Button edit;

  protected volatile InformationPreviewItem selectedItem;
  protected final Map<String, UmlDiagramInfo> infos = new ConcurrentHashMap<>();
  protected final Map<String, Image> previews = new ConcurrentHashMap<>();
  protected final UmlDiagramRender render = new UmlDiagramRender();

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  public GridPane show(InformationPreviewItem item) {
    this.selectedItem = item;
    Image image = previews.get(item.getName());
    if (image != null) {
      imageView.setImage(image);
      adjustImageViewSize();
    }
    return root;
  }

  private void adjustImageViewSize() {
    double width = imageContainer.getWidth() - 20;
    double height = imageContainer.getHeight() - 20;
    width = width < 0 ? 650 : width;
    height = height < 0 ? 700 : height;
    imageView.setFitWidth(width);
    imageView.setFitHeight(height);
    log.info("New fit size: w={}, h={}", imageView.getFitWidth(), imageView.getFitHeight());
  }

  @Override
  public void edit() {
    onEdit();
  }

  @FXML
  void onEdit() {
    ActivityHint activityHint = new ActivityHint(UmlDiagramActivity.class, controller.getCurrentActivityId());
    String name = selectedItem.getName();
    UmlDiagramInfo diagramInfo = infos.get(name);
    infos.remove(name);

    activityHint.setDataSourceHint(() -> diagramInfo);
    controller.startOrResume(activityHint);
  }

  @Override
  protected void onRefresh(List<InformationPreviewItem> model) {
    adjustImageViewSize();
    infos.clear();
    model.stream()//
      .filter(preview -> preview.getType().equals(UmlDiagramInfo.class))//
      .forEach(this::load);
  }

  private CompletableFuture<Void> load(InformationPreviewItem preview) {
    return CompletableFuture.supplyAsync(() -> PersistentWork.forName(UmlDiagramInfo.class, preview.getName()), controller.getExecutorService())//
      .thenApply(diagram -> {
        Path imagePath = getImagePath(diagram.getName());
        if (imagePath.toFile().exists()) {
          return imagePath.toFile();
        } else {
          File file = render.genereateSvg(diagram.getContent(), 800, imagePath);
          file.deleteOnExit();
          return file;
        }
      })//
      .thenApply(file -> {
        if (file != null) {
          Image image = Images.get(file.getPath());
          previews.put(preview.getName(), image);
          return image;
        } else {
          return null;
        }
      })//
      .thenAcceptAsync(image -> {
        if (image != null && selectedItem != null && preview.getName().equals(selectedItem.getName())) {
          imageView.setImage(image);
          adjustImageViewSize();
        }
      }, controller.getJavaFXExecutor())//
      .exceptionally(e -> {
        log.error("Could not render preview of umlDiagram {}", preview.getName(), e);
        return null;
      });
  }

  private Path getImagePath(String fileName) {
    return Paths.get(System.getProperty("java.io.tmpdir"), fileName + ".png");
  }
}
