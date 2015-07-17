/**
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.gallery.ui;

import de.ks.BaseController;
import de.ks.gallery.ui.thumbnail.ThumbnailGallery;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GalleryController extends BaseController<Object> {
  private static final Logger log = LoggerFactory.getLogger(GalleryController.class);
  @FXML
  private VBox favoriteContainer;
  @FXML
  private StackPane thumbnailContainer;
  @FXML
  private SplitPane root;
  @FXML
  private TreeView<File> fileView;

  private ThumbnailGallery thumbnailGallery;
  private boolean showHidden;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    thumbnailGallery = activityInitialization.loadAdditionalController(ThumbnailGallery.class).getController();
    thumbnailContainer.getChildren().add(thumbnailGallery.getRoot());
    thumbnailGallery.getLoader().setVisible(false);

    TreeItem<File> root = createRoot();
    fileView.setRoot(root);
    fileView.setShowRoot(false);
    fileView.setCellFactory(new Callback<TreeView<File>, TreeCell<File>>() {
      @Override
      public TreeCell<File> call(TreeView<File> param) {
        return new TreeCell<File>() {
          @Override
          protected void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
              setText("");
            } else {
              String name = item.getName();
              name = name.isEmpty() ? "/" : name;
              setText(name);
            }
          }
        };
      }
    });
    fileView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null && n != o) {
        thumbnailGallery.setFolder(n.getValue(), false);
      }
    });
  }

  protected TreeItem<File> createRoot() {
    Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
    TreeItem<File> root = new TreeItem<>(null);
    for (Path rootDirectory : rootDirectories) {
      TreeItem<File> subRoot = new TreeItem<>(rootDirectory.toFile());
      subRoot.expandedProperty().addListener((p, o, n) -> expand(subRoot, true));
      root.getChildren().add(subRoot);

      expand(subRoot, false);
    }
    return root;
  }

  private void expand(TreeItem<File> parent, boolean childExpansion) {
    ArrayList<TreeItem<File>> roots = new ArrayList<TreeItem<File>>();
    if (childExpansion) {
      roots.addAll(parent.getChildren());
    } else {
      roots.add(parent);
    }
    for (TreeItem<File> root : roots) {
      File[] array = root.getValue().listFiles(pathname -> pathname.isDirectory());
      List<File> files = array == null ? Collections.emptyList() : Arrays.asList(array);
      files.remove(null);
      Collections.sort(files);

      if (files != null) {
        for (File file : files) {
          try {
            boolean hidden = Files.isHidden(file.toPath());
            boolean add = (hidden && showHidden) || !hidden;
            if (add) {
              TreeItem<File> item = new TreeItem<>(file);
              item.expandedProperty().addListener((p, o, n) -> expand(item, true));
              ObservableList<TreeItem<File>> children = root.getChildren();
              children.add(item);
              log.trace("Adding {} to {}", item.getValue().getName(), root.getValue().getName());
            }
          } catch (IOException e) {
            log.error("Could not check file {}", file, e);
          }
        }
      }
    }
  }
}
