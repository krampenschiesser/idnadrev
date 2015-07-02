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
package de.ks.blogging.grav.ui.post.edit;

import de.ks.BaseController;
import de.ks.blogging.grav.posts.BasePost;
import de.ks.blogging.grav.posts.Header;
import de.ks.blogging.grav.posts.HeaderContainer;
import de.ks.blogging.grav.posts.HeaderElement;
import de.ks.reflection.PropertyPath;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.function.Consumer;

public class AdvancedHeader extends BaseController<BasePost> {
  @FXML
  protected StackPane root;
  @FXML
  protected TreeTableColumn<HeaderElement, String> keyColumn;
  @FXML
  protected TreeTableColumn<HeaderElement, String> valueColumn;
  @FXML
  protected TreeTableView<HeaderElement> table;

  protected final List<Consumer<BasePost>> actions = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
    valueColumn.setEditable(true);
    table.setEditable(true);

    keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>(PropertyPath.property(HeaderElement.class, c -> c.getKey())));
    valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>(PropertyPath.property(HeaderElement.class, c -> c.getValue())));
    valueColumn.setOnEditCommit(e -> {
      String newValue = e.getNewValue();
      HeaderElement headerElement = e.getRowValue().getValue();
      HeaderContainer owner = headerElement.getOwner();
      actions.add(post -> {
        HeaderContainer resolved = null;
        if (headerElement.getKey() == null) {
          resolved = post.getHeader();
        } else {
          Stack<HeaderContainer> headerContainers = new Stack<>();
          headerContainers.addAll(post.getHeader().getChildContainers());
          while (resolved == null && !headerContainers.isEmpty()) {
            HeaderContainer container = headerContainers.pop();
            if (container.equals(owner)) {
              resolved = container;
            }
            headerContainers.addAll(container.getChildContainers());
          }
        }
        resolved.setHeaderElement(headerElement.getKey(), newValue);
      });
    });
  }

  @Override
  public void duringSave(BasePost model) {
    this.actions.forEach(a -> a.accept(model));
    this.actions.clear();
  }

  @Override
  protected void onRefresh(BasePost model) {
    Header header = model.getHeader();

    TreeItem<HeaderElement> root = new TreeItem<>(new HeaderElement("/", "", header));
    root.setExpanded(true);
    addContainerToTable(header, root);
    table.setRoot(root);
  }

  @Override
  public void onSuspend() {
    actions.clear();
  }

  private void addContainerToTable(HeaderContainer header, TreeItem<HeaderElement> root) {
    for (HeaderElement headerElement : header.getHeaderElements()) {
      root.getChildren().add(new TreeItem<>(headerElement));
    }
    List<HeaderContainer> childContainers = header.getChildContainers();
    for (HeaderContainer childContainer : childContainers) {
      TreeItem<HeaderElement> childRoot = new TreeItem<>(new HeaderElement(childContainer.getKey(), "", header));
      root.getChildren().add(childRoot);
      addContainerToTable(childContainer, childRoot);
    }
  }
}
