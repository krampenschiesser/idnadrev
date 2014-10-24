package de.ks.idnadrev.category.create;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.file.FileStore;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.entity.FileReference;
import de.ks.idnadrev.entity.TaskTest;
import de.ks.idnadrev.review.weeklydone.WeeklyDoneActivity;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import javafx.scene.paint.Color;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class CreateCategoryActivityTest extends ActivityTest {
  @Inject
  FileStore fileStore;
  private CreateCategoryController controller;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return CreateCategoryActivity.class;
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(CreateCategoryController.class);
  }

  @Test
  public void testCreateNew() throws Exception {
    assertTrue(controller.noImageLabel.isVisible());
    assertTrue(controller.save.isDisabled());
    assertNull(controller.imageView.getImage());

    FXPlatform.invokeLater(() -> {
      controller.name.setText("category");
      controller.colorSelection.setValue(Color.AQUA);
      try {
        File file = getDoneFile();
        controller.selectImage(file);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    assertNotNull(controller.imageView.getImage());
    assertFalse(controller.save.isDisabled());
    assertFalse(controller.noImageLabel.isVisible());

    controller.onSave();
    activityController.waitForDataSource();

    assertNull(controller.name.getText());
    PersistentWork.wrap(() -> {
      Category category = PersistentWork.from(Category.class).get(0);
      assertEquals("category", category.getName());
      assertEquals(Color.AQUA, category.getColor());
      assertNotNull(category.getImage());
      assertEquals("done.png", category.getImage().getName());
    });
  }

  private File getDoneFile() {
    try {
      return new File(WeeklyDoneActivity.class.getResource("done.png").toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private File getThePctFile() {
    try {
      return new File(TaskTest.class.getResource("img.jpg").toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testEdit() throws Exception {
    store.getDatasource().setLoadingHint(createCategory());
    activityController.reload();
    activityController.waitForDataSource();

    assertEquals("category", controller.name.getText());
    assertEquals(Color.RED, controller.colorSelection.getValue());
    assertNotNull(controller.imageView.getImage());

    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      controller.colorSelection.setValue(Color.ALICEBLUE);
      controller.onSave();
    });
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      Category category = PersistentWork.from(Category.class).get(0);
      assertEquals("test", category.getName());
      assertEquals(Color.ALICEBLUE, category.getColor());
      assertNotNull(category.getImage());
      assertEquals("done.png", category.getImage().getName());
    });
  }

  @Test
  public void testEditImage() throws Exception {
    store.getDatasource().setLoadingHint(createCategory());
    activityController.reload();
    activityController.waitForDataSource();


    FXPlatform.invokeLater(() -> {
      try {
        File file = getThePctFile();
        controller.selectImage(file);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      controller.onSave();
    });
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      Category category = PersistentWork.from(Category.class).get(0);
      assertNotNull(category.getImage());
      assertEquals("img.jpg", category.getImage().getName());
    });
  }

  private Category createCategory() {
    return PersistentWork.read(em -> {
      try {
        File doneFile = getDoneFile();
        FileReference fileReference = fileStore.getReference(doneFile).get();
        fileStore.scheduleCopy(fileReference, doneFile);
        Category category = new Category("category").setColorAsString("#FF0000").setImage(fileReference);
        em.persist(category);
        return category;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }
}