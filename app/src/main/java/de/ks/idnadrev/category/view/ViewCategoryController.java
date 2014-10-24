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
package de.ks.idnadrev.category.view;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.application.fxml.DefaultLoader;
import de.ks.idnadrev.category.CategoryBrowser;
import de.ks.idnadrev.category.CategoryItemController;
import de.ks.idnadrev.category.create.CreateCategoryActivity;
import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.entity.information.Information;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ViewCategoryController extends BaseController<List<Category>> {
  private static final Logger log = LoggerFactory.getLogger(ViewCategoryController.class);
  private static final String KEY_CATEGORY = PropertyPath.property(Information.class, t -> t.getCategory());

  @FXML
  protected CategoryBrowser categoryBrowserController;
  @FXML
  protected ScrollPane scrollPane;
  @FXML
  protected StackPane selectedItemContainer;
  @FXML
  protected Button create;
  @FXML
  protected Button edit;
  @FXML
  protected Button delete;
  private CompletableFuture<DefaultLoader<Node, CategoryItemController>> categoryItemFuture;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    categoryItemFuture = activityInitialization.loadAdditionalControllerWithFuture(CategoryItemController.class);
    categoryBrowserController.getCategoryPane().prefWidthProperty().bind(scrollPane.widthProperty().subtract(50));
    categoryBrowserController.getCategoryPane().prefHeightProperty().bind(scrollPane.heightProperty().subtract(50));
    categoryBrowserController.selectedCategoryProperty().addListener((p, o, n) -> {

      selectedItemContainer.getChildren().clear();
      if (n == null) {
      } else {
        CategoryItemController itemController = getItemController();
        itemController.setCategory(n);
        selectedItemContainer.getChildren().add(itemController.getPane());
        StackPane.setAlignment(itemController.getPane(), Pos.CENTER_LEFT);
      }
    });

    BooleanBinding noSelection = categoryBrowserController.selectedCategoryProperty().isNull();
    ReadOnlyBooleanProperty invalidProperty = validationRegistry.invalidProperty();
    edit.disableProperty().bind(noSelection.or(invalidProperty));
    delete.disableProperty().bind(noSelection.or(invalidProperty));
    create.disableProperty().bind(invalidProperty);
  }

  protected CategoryItemController getItemController() {
    DefaultLoader<Node, CategoryItemController> loader = null;
    try {
      loader = categoryItemFuture.get();
      return loader.getController();
    } catch (InterruptedException e) {
      log.error("Could not load category item controller", e);
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      log.error("Could not load category item controller", e);
      throw new RuntimeException(e);
    }
  }

  @FXML
  protected void onCreate() {
    ActivityHint hint = new ActivityHint(CreateCategoryActivity.class, controller.getCurrentActivityId());
    controller.startOrResume(hint);
  }

  @FXML
  protected void onEdit() {
    Category item = categoryBrowserController.getSelectedCategory();

    ActivityHint hint = new ActivityHint(CreateCategoryActivity.class, controller.getCurrentActivityId());
    hint.setDataSourceHint(() -> item);

    controller.startOrResume(hint);
  }

  @FXML
  protected void onDelete() {
    Category item = categoryBrowserController.getSelectedCategory();
    store.executeCustomRunnable(() -> {
      PersistentWork.run(em -> {
        Category reload = PersistentWork.reload(item);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        deleteReferences(em, reload, builder, TextInfo.class);
        deleteReferences(em, reload, builder, UmlDiagramInfo.class);
        deleteReferences(em, reload, builder, ChartInfo.class);
        em.flush();
        em.remove(reload);
      });
    });
    store.reload();
    store.executeCustomRunnable(() -> controller.getJavaFXExecutor().submit(() -> categoryBrowserController.onResume()));
  }

  protected <T extends Information<T>> void deleteReferences(EntityManager em, Category reload, CriteriaBuilder builder, Class<T> clazz) {
    CriteriaUpdate<T> update = builder.createCriteriaUpdate(clazz);
    Root<T> root = update.from(clazz);
    Path<Category> contextPath = root.get(KEY_CATEGORY);
    update.set(contextPath, builder.nullLiteral(Category.class));
    update.where(builder.equal(contextPath, reload));

    em.createQuery(update).executeUpdate();
  }

  @Override
  protected void onRefresh(List<Category> model) {
    super.onRefresh(model);
    categoryBrowserController.reload();
    categoryBrowserController.setSelectedCategory(null);
  }
}
