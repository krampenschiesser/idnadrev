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
import de.ks.persistence.PersistentWork;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
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
  public static final int DEFAULT_WIDTH = 650;
  public static final int DEFAULT_HEIGHT = 700;
  @FXML
  protected ImageView imageView;
  @FXML
  protected StackPane imageContainer;

  protected volatile InformationPreviewItem selectedItem;
  protected final Map<String, UmlDiagramInfo> infos = new ConcurrentHashMap<>();
  protected final Map<String, Image> previews = new ConcurrentHashMap<>();
  protected final UmlDiagramRender render = new UmlDiagramRender();

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @Override
  public Pane show(InformationPreviewItem item) {
    this.selectedItem = item;
    Image image = previews.get(item.getName());
    if (image != null) {
      imageView.setImage(image);
      adjustImageViewSize();
    }
    return imageContainer;
  }

  private void adjustImageViewSize() {
    double width = imageContainer.getWidth() - 20;
    double height = imageContainer.getHeight() - 20;
    width = width < 0 ? DEFAULT_WIDTH : width;
    height = height < 0 ? DEFAULT_HEIGHT : height;
    imageView.setFitWidth(width);
    imageView.setFitHeight(height);
  }

  @Override
  public void edit() {
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
        infos.put(diagram.getName(), diagram);
        Path imagePath = getImagePath(diagram.getName());
        if (imagePath.toFile().exists()) {
          imagePath.toFile().delete();
        }
        File file = render.generatePng(diagram.getContent(), Math.max(DEFAULT_WIDTH, this.imageContainer.getWidth() - 20), imagePath);
        file.deleteOnExit();
        return file;
      })//
      .thenApply(file -> {
        if (file != null) {

          try {
            URL url = file.toURI().toURL();
            Image image = new Image(url.toExternalForm());
            previews.put(preview.getName(), image);
            return image;
          } catch (MalformedURLException e) {
            throw new RuntimeException(e);
          }
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
