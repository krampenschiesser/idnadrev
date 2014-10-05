package de.ks.idnadrev.category;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Category;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class CategoryBrowserTest extends ActivityTest {
  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return CategoryBrowseTestActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    em.persist(new Category("hello"));
    em.persist(new Category("world"));
  }

  @Test
  public void testFlowPaneFilling() throws Exception {
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    CategoryBrowser browser = activityController.getControllerInstance(CategoryBrowser.class);
    assertEquals(2, browser.categoryPane.getChildren().size());

    PersistentWork.deleteAllOf(Category.class);
    PersistentWork.persist(new Category("hello"));

    browser.onResume();
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(1, browser.categoryPane.getChildren().size());

    PersistentWork.persist(new Category("sauerland"));
    browser.onResume();
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(2, browser.categoryPane.getChildren().size());
  }

  @Test
  public void testCategoryItem() throws Exception {
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    CategoryBrowser controllerInstance = activityController.getControllerInstance(CategoryBrowser.class);
    CategoryItemController first = controllerInstance.itemControllers.get(0);
    CategoryItemController second = controllerInstance.itemControllers.get(1);
    assertEquals("hello", first.title.getText());
    assertEquals("world", second.title.getText());

    assertNull(controllerInstance.selectedCategory.get());
    FXPlatform.invokeLater(() -> first.getPane().getOnMouseClicked().handle(null));

    assertNotNull(controllerInstance.selectedCategory.get());
    assertEquals("hello", controllerInstance.selectedCategory.get().getName());
  }
}