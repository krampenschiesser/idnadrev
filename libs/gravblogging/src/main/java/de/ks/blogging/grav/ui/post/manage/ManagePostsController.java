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
import de.ks.blogging.grav.posts.BasePost;
import de.ks.executor.group.LastTextChange;
import de.ks.i18n.Localized;
import de.ks.javafx.event.ClearTextOnEscape;
import de.ks.markdown.viewer.MarkdownContent;
import de.ks.markdown.viewer.MarkdownViewer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManagePostsController extends BaseController<List<BasePost>> {
  @FXML
  private TableView<BasePost> postTable;
  @FXML
  private TableColumn<BasePost, String> titleColumn;
  @FXML
  private TableColumn<BasePost, LocalDateTime> dateColumn;

  @FXML
  private Button edit;
  @FXML
  private Button create;
  @FXML
  private Button delete;

  @FXML
  private StackPane previewContainer;
  @FXML
  private TextField titleSearch;

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Localized.get("fullDate"));
  private LastTextChange lastTextChange;
  private ObservableList<BasePost> items;

  private MarkdownViewer markdownViewer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    MarkdownViewer.load(node -> previewContainer.getChildren().add(node), ctrl -> markdownViewer = ctrl);

    titleColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getHeader().getTitle()));
    dateColumn.setCellValueFactory(param -> new SimpleObjectProperty<LocalDateTime>(param.getValue().getHeader().getLocalDateTime().orElseGet(() -> LocalDateTime.now())));

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
    delete.disableProperty().bind(nothingSelected);

    postTable.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n == null) {
        markdownViewer.reset();
      } else {
        markdownViewer.show(createMarkdownContent(n));
      }
    });
  }

  @FXML
  public void onCreate() {

  }

  @FXML
  public void onEdit() {

  }

  @FXML
  public void onDelete() {

  }

  @Override
  protected void onRefresh(List<BasePost> model) {
    markdownViewer.preload(model.stream().map(this::createMarkdownContent).collect(Collectors.toList()));

    controller.getJavaFXExecutor().submit(() -> {
      items = FXCollections.observableArrayList(model);
      applyItems();
    });
  }

  private MarkdownContent createMarkdownContent(BasePost m) {
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
