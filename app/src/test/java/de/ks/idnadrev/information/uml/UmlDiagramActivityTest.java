package de.ks.idnadrev.information.uml;

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.UmlDiagramInfo;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class UmlDiagramActivityTest extends ActivityTest {
  public static final String UML_DIAGRAM_STRING = "\n" +
    "Class01 \"1\" *-- \"many\" Class02 : contains\n" +
    "Class03 o-- Class04 : aggregation\n" +
    "Class05 --> \"1\" Class06";

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());

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
  protected void createTestData(Session session) {
  }

  @Test
  public void testCreateNew() throws Exception {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      controller.content.setText(UML_DIAGRAM_STRING);
      controller.tagContainerController.addTag("tag1");
    });
    FXPlatform.waitForFX();
    activityController.waitForTasks();
    activityController.save();
    activityController.waitForDataSource();

    persistentWork.run(session -> {
      UmlDiagramInfo diagramInfo = persistentWork.forName(UmlDiagramInfo.class, "test");
      assertNotNull(diagramInfo);


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
    UmlDiagramInfo model = persistentWork.read(em -> {
      UmlDiagramInfo textInfo = new UmlDiagramInfo("test").setContent(UML_DIAGRAM_STRING);
      textInfo.addTag("tag");
      em.persist(textInfo);
      return textInfo;
    });
    datasource.setLoadingHint(model);

    activityController.reload();
    activityController.waitForDataSource();

    assertEquals("test", controller.name.getText());
    assertEquals(UML_DIAGRAM_STRING, controller.content.getText());
    assertTrue(controller.tagContainerController.getCurrentTags().contains("tag"));

    FXPlatform.invokeLater(() -> controller.content.setText("other"));
    activityController.save();
    activityController.waitForDataSource();

    persistentWork.run(session -> {
      List<UmlDiagramInfo> from = persistentWork.from(UmlDiagramInfo.class);
      assertEquals(1, from.size());
      UmlDiagramInfo info = from.get(0);
      assertEquals("other", info.getContent());
    });
  }
}