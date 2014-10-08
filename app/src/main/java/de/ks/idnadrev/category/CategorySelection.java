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

import de.ks.activity.initialization.ActivityInitialization;
import de.ks.application.fxml.DefaultLoader;
import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.selection.BaseNamedPersistentObjectSelection;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class CategorySelection extends BaseNamedPersistentObjectSelection<Category> {
  private static final Logger log = LoggerFactory.getLogger(CategorySelection.class);

  @Inject
  protected ActivityInitialization initialization;

  protected CategoryBrowser categoryBrowser;
  protected Node categoryView;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    DefaultLoader<Node, CategoryBrowser> loader = initialization.loadAdditionalController(CategoryBrowser.class);
    categoryBrowser = loader.getController();
    categoryView = loader.getView();

    categoryBrowser.selectedCategory.addListener((p, o, n) -> {
      if (n != null) {
        selectedValue.set(n);
      }
    });
    this.from(Category.class);
  }

  @Override
  protected Node getBrowseNode() {
    return categoryView;
  }
}
