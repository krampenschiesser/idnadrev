/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks.idnadrev.category;

import com.google.common.eventbus.Subscribe;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class CategorySelection extends BaseNamedPersistentObjectSelection<Category> implements DatasourceCallback<Categorized> {
  private static final Logger log = LoggerFactory.getLogger(CategorySelection.class);

  @Inject
  protected ActivityInitialization initialization;

  protected CategoryBrowser categoryBrowser;
  protected FlowPane categoryView;
  private boolean clearOnRefresh = false;
  private boolean readOnly = false;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    DefaultLoader<Node, CategoryBrowser> loader = initialization.loadAdditionalController(CategoryBrowser.class);
    categoryBrowser = loader.getController();
    categoryView = (FlowPane) loader.getView();

    categoryBrowser.selectedCategory.addListener((p, o, n) -> {
      if (n != null) {
        selectedValue.set(n);
        controller.getJavaFXExecutor().submit(() -> hidePopup());
      }
    });
    this.from(Category.class);
  }

  @Override
  protected Node getBrowseNode() {
    ScrollPane scrollPane = new ScrollPane(categoryView);
    categoryView.prefWidthProperty().bind(scrollPane.widthProperty());
    categoryView.prefHeightProperty().bind(scrollPane.heightProperty());
    scrollPane.setPrefSize(500, 400);
    return scrollPane;
  }

  @Override
  public void duringLoad(Categorized model) {
    //
  }

  @Override
  public void duringSave(Categorized model) {
    if (!readOnly) {
      if (getSelectedValue() != null) {
        model.setCategory(PersistentWork.reload(getSelectedValue()));
      } else {
        model.setCategory(null);
      }
    }
  }

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  private void afterRefresh(ActivityLoadFinishedEvent e) {
    if (clearOnRefresh) {
      getInput().setText("");
    } else if (e.getModel() instanceof Categorized) {
      Category category = ((Categorized) e.getModel()).getCategory();
      if (category == null) {
        getInput().setText("");
      } else {
        getInput().setText(category.getName());
      }
    }
  }

  public void setClearOnRefresh(boolean clearOnRefresh) {
    this.clearOnRefresh = clearOnRefresh;
  }

  public boolean isClearOnRefresh() {
    return clearOnRefresh;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isReadOnly() {
    return readOnly;
  }
}
