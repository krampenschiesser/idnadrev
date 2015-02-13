package de.ks.idnadrev.information.uml;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class UmlDiagramActivityTest extends ActivityTest {

  public static final String UML_DIAGRAM_STRING = "\n" +
    "Class01 \"1\" *-- \"many\" Class02 : contains\n" +
    "Class03 o-- Class04 : aggregation\n" +
    "Class05 --> \"1\" Class06";
  private UmlDiagramController controller;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return UmlDiagramActivity.class;
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(UmlDiagramController.class);
    FXPlatform.invokeLater(() -> controller.getPersistentStoreBack().delete());
  }

  @Override
  protected void createTestData(EntityManager em) {
    em.persist(new Category("cat1"));
  }

  @Test
  public void testCreateNew() throws Exception {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      controller.content.setText(UML_DIAGRAM_STRING);
      controller.tagContainerController.addTag("tag1");
      controller.categorySelectionController.setSelectedValue(PersistentWork.forName(Category.class, "cat1"));
    });
    FXPlatform.waitForFX();
    activityController.waitForTasks();
    activityController.save();
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      UmlDiagramInfo diagramInfo = PersistentWork.forName(UmlDiagramInfo.class, "test");
      assertNotNull(diagramInfo);

      assertNotNull(diagramInfo.getCategory());
      assertEquals("cat1", diagramInfo.getCategory().getName());

      Set<Tag> tags = diagramInfo.getTags();
      assertEquals(1, tags.size());
      Tag tag = tags.iterator().next();
      assertEquals("tag1", tag.getName());
      assertEquals(UML_DIAGRAM_STRING, diagramInfo.getContent());
    });
  }

  @Test
  public void testEdit() throws Exception {
    UmlDiagramDS datasource = (UmlDiagramDS) store.getDatasource();
    UmlDiagramInfo model = PersistentWork.read(em -> {
      UmlDiagramInfo textInfo = new UmlDiagramInfo("test").setContent(UML_DIAGRAM_STRING);
      textInfo.addTag("tag");
      Category testCategory = new Category("testCategory");
      em.persist(testCategory);
      textInfo.setCategory(testCategory);
      em.persist(textInfo);
      return textInfo;
    });
    datasource.setLoadingHint(model);

    activityController.reload();
    activityController.waitForDataSource();

    assertEquals("test", controller.name.getText());
    assertEquals(UML_DIAGRAM_STRING, controller.content.getText());
    assertEquals("testCategory", controller.categorySelectionController.getInput().getText());
    assertTrue(controller.tagContainerController.getCurrentTags().contains("tag"));

    FXPlatform.invokeLater(() -> controller.content.setText("other"));
    activityController.save();
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      List<UmlDiagramInfo> from = PersistentWork.from(UmlDiagramInfo.class);
      assertEquals(1, from.size());
      UmlDiagramInfo info = from.get(0);
      assertEquals("other", info.getContent());
    });
  }
}