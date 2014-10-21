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
package de.ks.idnadrev.category;

import de.ks.BaseController;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.application.fxml.DefaultLoader;
import de.ks.idnadrev.entity.Category;
import de.ks.persistence.PersistentWork;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class CategoryBrowser extends BaseController<Object> {
  private static final Logger log = LoggerFactory.getLogger(CategoryBrowser.class);
  @FXML
  protected FlowPane categoryPane;

  protected final List<CategoryItemController> itemControllers = new ArrayList<>();
  protected final SimpleObjectProperty<Category> selectedCategory = new SimpleObjectProperty<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  @Override
  public void onStart() {
    onResume();
  }

  @Override
  public void onResume() {
    CompletableFuture.supplyAsync(this::readCategories, controller.getExecutorService())//
      .thenApply(this::loadCategoryItemControllers)//
      .thenAcceptAsync(controllers -> {
        controllers.forEach(c -> categoryPane.getChildren().add(c.getPane()));
      }, controller.getJavaFXExecutor());
  }

  protected List<Category> readCategories() {
    List<Category> from = PersistentWork.from(Category.class);
    log.debug("Found {} categories", from.size());
    return from;
  }

  public void reload() {
    itemControllers.clear();
    onResume();
  }

  protected List<CategoryItemController> loadCategoryItemControllers(List<Category> allCategories) {
    List<Category> categories = new ArrayList<>(allCategories);
    List<Category> alreadyLoadedCategories = itemControllers.stream().map(ic -> ic.getCategory()).collect(Collectors.toList());
    log.debug("Found {} already loaded categories", alreadyLoadedCategories.size());

    categories.removeAll(alreadyLoadedCategories);
    log.debug("Found {} additional categories", categories.size());

    List<Category> removedCategories = alreadyLoadedCategories.stream().filter(cat -> !allCategories.contains(cat)).collect(Collectors.toList());
    log.debug("Found {} removed categories", removedCategories.size());
    List<CategoryItemController> removed = itemControllers.stream().filter(ic -> removedCategories.contains(ic.getCategory())).collect(Collectors.toList());

    itemControllers.removeAll(removed);
    Future<?> submit = controller.getJavaFXExecutor().submit(() -> removed.forEach(ic -> categoryPane.getChildren().remove(ic.getPane())));
    try {
      submit.get();
    } catch (Exception e) {
      log.error("Could not execute fx executor runnable", e);
    }


    ActivityExecutor executorService = controller.getExecutorService();
    List<Future<CategoryItemController>> futures = categories.stream().map(category -> executorService.submit(() -> loadSingleItemController(category))).collect(Collectors.toList());

    List<CategoryItemController> categoryItemControllers = futures.stream().map(f -> {
      try {
        CategoryItemController itemController = f.get();
        return itemController;
      } catch (InterruptedException e) {
        return null;
      } catch (ExecutionException e) {
        log.error("Could not load {}", CategoryItemController.class.getName(), e);
        return null;
      }
    }).collect(Collectors.toList());
    itemControllers.addAll(categoryItemControllers);
    return categoryItemControllers;
  }

  protected CategoryItemController loadSingleItemController(Category category) {
    try {
      DefaultLoader<Node, CategoryItemController> loader = activityInitialization.loadAdditionalControllerWithFuture(CategoryItemController.class).get();
      CategoryItemController categoryItemController = loader.getController();
      categoryItemController.setCategory(category);
      categoryItemController.setSelectionProperty(selectedCategory);
      return categoryItemController;
    } catch (Exception e) {
      log.error("Could not load {}", CategoryItemController.class.getName(), e);
      return null;
    }
  }

  public FlowPane getCategoryPane() {
    return categoryPane;
  }

  public Category getSelectedCategory() {
    return selectedCategory.get();
  }

  public SimpleObjectProperty<Category> selectedCategoryProperty() {
    return selectedCategory;
  }

  public void setSelectedCategory(Category selectedCategory) {
    this.selectedCategory.set(selectedCategory);
  }
}
