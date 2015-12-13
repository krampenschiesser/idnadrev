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

import de.ks.flatjsondb.PersistentWork;
import de.ks.gallery.entity.GalleryFavorite;
import de.ks.gallery.ui.thumbnail.ThumbnailGallery;
import de.ks.standbein.BaseController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
  protected VBox favoriteContainer;
  @FXML
  protected StackPane thumbnailContainer;
  @FXML
  protected StackPane markedContainer;
  @FXML
  protected SplitPane root;
  @FXML
  protected TreeView<File> fileView;

  @FXML
  protected ToggleButton showHidden;
  @FXML
  protected Button markFavorite;

  protected ThumbnailGallery thumbnailGallery;
  protected MarkdedItemController markedItemController;

  @Inject
  PersistentWork persistentWork;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    thumbnailGallery = activityInitialization.loadAdditionalController(ThumbnailGallery.class).getController();
    thumbnailContainer.getChildren().add(thumbnailGallery.getRoot());
    thumbnailGallery.hideLoader();

    markedItemController = activityInitialization.loadAdditionalController(MarkdedItemController.class).getController();
    markedContainer.getChildren().add(markedItemController.getRoot());
    markedItemController.bindTo(thumbnailGallery.getSlideshow());


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
      subRoot.expandedProperty().addListener((p, o, n) -> {
        if (n) {
          expand(subRoot, true);
        }
      });
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

      for (File file : files) {
        try {
          boolean hidden = Files.isHidden(file.toPath());
          boolean add = (hidden && showHidden.isSelected()) || !hidden;
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

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    reloadFavorites();
  }

  protected void reloadFavorites() {
    List<GalleryFavorite> favorites = persistentWork.from(GalleryFavorite.class);
    Collections.sort(favorites, Comparator.comparing(f -> f.getName()));

    controller.getJavaFXExecutor().submit(() -> {
      favoriteContainer.getChildren().clear();
      for (GalleryFavorite favorite : favorites) {
        GridPane container = new GridPane();
        container.getColumnConstraints().add(new ColumnConstraints(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, true));
        container.getColumnConstraints().add(new ColumnConstraints(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.NEVER, HPos.LEFT, true));

        Hyperlink link = new Hyperlink(favorite.getName());
        link.setOnAction(e -> {
          selectPath(favorite.getFolderPath());
        });
        container.add(link, 0, 0);

        Button delete = new Button("(x)");
        delete.setOnAction(e -> {
          persistentWork.remove(favorite);
          reloadFavorites();
        });

        container.add(delete, 1, 0);
        favoriteContainer.getChildren().add(container);
      }
    });
  }

  protected void selectPath(File folderPath) {
    collapseAll();
    Path path = folderPath.toPath();
    Optional<TreeItem<File>> first = Optional.empty();
    for (TreeItem<File> root : fileView.getRoot().getChildren()) {
      for (Path path1 : path) {
        first = root.getChildren().stream().filter(c -> {
          String pathName = path1.getFileName().toString();
          String name = c.getValue().getName();
          return name.equals(pathName);
        }).findFirst();
        if (first.isPresent()) {
          root.setExpanded(true);
          TreeItem<File> fileTreeItem = first.get();
          fileTreeItem.setExpanded(true);
          root = first.get();
        }
      }
    }
    if (first.isPresent()) {
      fileView.getSelectionModel().select(first.get());
      int index = fileView.getSelectionModel().getSelectedIndex();
      fileView.getFocusModel().focus(index);
      fileView.scrollTo(index);
    }
  }

  @FXML
  void onMarkFavorite() {
    TreeItem<File> selectedItem = fileView.getSelectionModel().getSelectedItem();

    if (selectedItem != null) {
      persistentWork.persist(new GalleryFavorite(selectedItem.getValue()));
      reloadFavorites();
    }
  }

  @FXML
  void onShowHidden() {
    TreeItem<File> selectedItem = fileView.getSelectionModel().getSelectedItem();
    TreeItem<File> root = createRoot();
    fileView.setRoot(root);

    if (selectedItem != null) {
      selectPath(selectedItem.getValue());
    }
  }

  private void collapseAll() {
    TreeItem<File> root = fileView.getRoot();
    collapse(root);
  }

  private void collapse(TreeItem<File> root) {
    root.setExpanded(false);
    root.getChildren().forEach(c -> collapse(c));
  }
}
