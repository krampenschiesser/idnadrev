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
package de.ks.blogging.grav.ui.post.manage;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.posts.BasePost;
import de.ks.blogging.grav.ui.post.edit.CreateEditPostActivity;
import de.ks.executor.group.LastTextChange;
import de.ks.i18n.Localized;
import de.ks.javafx.event.ClearTextOnEscape;
import de.ks.markdown.viewer.MarkdownContent;
import de.ks.markdown.viewer.MarkdownViewer;
import de.ks.persistence.PersistentWork;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ManagePostsController extends BaseController<List<BasePost>> {
  private static final Logger log = LoggerFactory.getLogger(ManagePostsController.class);
  @FXML
  protected ChoiceBox<GravBlog> blogSelection;
  @FXML
  protected TableView<BasePost> postTable;
  @FXML
  protected TableColumn<BasePost, String> titleColumn;
  @FXML
  protected TableColumn<BasePost, LocalDateTime> dateColumn;

  @FXML
  protected Button edit;
  @FXML
  protected Button create;
  @FXML
  protected Button delete;
  @FXML
  protected Button showFolder;

  @FXML
  protected StackPane previewContainer;
  @FXML
  protected TextField titleSearch;

  protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Localized.get("fullDate"));
  protected final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
  protected LastTextChange lastTextChange;
  protected ObservableList<BasePost> items;

  protected MarkdownViewer markdownViewer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    MarkdownViewer.load(node -> previewContainer.getChildren().add(node), ctrl -> markdownViewer = ctrl);

    titleColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getHeader().getTitle()));
    dateColumn.setCellValueFactory(param -> new SimpleObjectProperty<LocalDateTime>(param.getValue().getHeader().getLocalDateTime().orElse(LocalDateTime.of(1970, 1, 1, 12, 42, 0))));

    dateColumn.setCellFactory(new Callback<TableColumn<BasePost, LocalDateTime>, TableCell<BasePost, LocalDateTime>>() {
      @Override
      public TableCell<BasePost, LocalDateTime> call(TableColumn<BasePost, LocalDateTime> param) {
        TableCell<BasePost, LocalDateTime> cell = new TableCell<BasePost, LocalDateTime>() {
          @Override
          protected void updateItem(LocalDateTime item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
              setText(formatter.format(item));
            } else {
              setText("");
            }
          }
        };
        return cell;
      }
    });


    titleSearch.setOnKeyReleased(new ClearTextOnEscape());
    lastTextChange = new LastTextChange(titleSearch, controller.getExecutorService());
    lastTextChange.registerHandler(cf -> cf.thenRunAsync(() -> applyItems(), controller.getJavaFXExecutor()));

    postTable.getSortOrder().add(dateColumn);
    dateColumn.setSortType(TableColumn.SortType.DESCENDING);
    dateColumn.setSortable(true);
    postTable.sort();

    BooleanBinding nothingSelected = postTable.getSelectionModel().selectedItemProperty().isNull();
    edit.disableProperty().bind(nothingSelected);
    showFolder.disableProperty().bind(nothingSelected);
    delete.disableProperty().bind(nothingSelected);

    postTable.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n == null) {
        markdownViewer.reset();
      } else {
        markdownViewer.show(createMarkdownContent(n));
      }
    });
    blogSelection.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      store.getDatasource().setLoadingHint(n);
      controller.reload();
    });
    blogSelection.setConverter(new StringConverter<GravBlog>() {
      @Override
      public String toString(GravBlog object) {
        return object.getName();
      }

      @Override
      public GravBlog fromString(String string) {
        return blogSelection.getItems().stream().filter(i -> i.getName().equals(string)).findFirst().get();
      }
    });
  }

  @FXML
  public void onCreate() {
    ActivityHint hint = new ActivityHint(CreateEditPostActivity.class);
    hint.returnToCurrent();
    hint.setDataSourceHint(() -> null);

    controller.startOrResume(hint);
  }

  @FXML
  public void onEdit() {
    ActivityHint hint = new ActivityHint(CreateEditPostActivity.class);
    hint.returnToCurrent();

    Supplier supplier = () -> postTable.getSelectionModel().getSelectedItem();
    hint.setReturnToDatasourceHint(supplier);
    hint.setDataSourceHint(supplier);

    controller.startOrResume(hint);
  }

  @FXML
  public void onShowFolder() {
    BasePost post = postTable.getSelectionModel().getSelectedItem();
    Thread thread = new Thread(() -> {
      try {
        desktop.open(post.getFile().getParentFile());
      } catch (Exception e) {
        log.error("Could not open {}", post.getFile(), e);
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  @FXML
  public void onDelete() {

  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    List<GravBlog> blogs = PersistentWork.from(GravBlog.class);
    controller.getJavaFXExecutor().submit(() -> {
      blogSelection.setItems(FXCollections.observableList(blogs));
      SingleSelectionModel<GravBlog> selectionModel = blogSelection.getSelectionModel();
      if (blogs.size() > 0 && selectionModel.isEmpty()) {
        selectionModel.select(0);
      }
    });
  }

  @Override
  protected void onRefresh(List<BasePost> model) {
    markdownViewer.preload(model.stream().map(this::createMarkdownContent).collect(Collectors.toList()));

    controller.getJavaFXExecutor().submit(() -> {
      items = FXCollections.observableArrayList(model);
      applyItems();
    });
  }

  protected MarkdownContent createMarkdownContent(BasePost m) {
    if (m.getFile() != null) {
      return new MarkdownContent(m.getHeader().getTitle(), m.getFile());
    } else {
      return new MarkdownContent(m.getHeader().getTitle(), m.getContent());
    }
  }

  protected void applyItems() {
    ObservableList<TableColumn<BasePost, ?>> oldOrder = FXCollections.observableArrayList(postTable.getSortOrder());
    postTable.getSortOrder().clear();
    TableColumn.SortType dateSortType = dateColumn.getSortType();
    TableColumn.SortType titleSortType = titleColumn.getSortType();

    List<BasePost> filtered = items.stream().filter(basePost -> basePost.getHeader().getTitle().toLowerCase(Locale.ROOT).contains(titleSearch.getText().toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    postTable.setItems(FXCollections.observableList(filtered));

    postTable.getSortOrder().addAll(oldOrder);
    dateColumn.setSortType(dateSortType);
    titleColumn.setSortType(titleSortType);
    dateColumn.setSortable(true);
    titleColumn.setSortable(true);
    postTable.sort();
  }
}
