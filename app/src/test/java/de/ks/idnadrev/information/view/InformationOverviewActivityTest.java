package de.ks.idnadrev.information.view;

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.Information;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InformationOverviewActivityTest extends ActivityTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();

  private InformationListView listView;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return InformationOverviewActivity.class;
  }

  @Before
  public void setUp() throws Exception {
    listView = activityController.getControllerInstance(InformationListView.class);
  }

  @Override
  protected void createTestData(Session session) {
    for (int i = 0; i < 5; i++) {
      Tag tag = new Tag("tag" + i);
      Information info = new Information("info" + i);
      info.addTag(tag);
      persistentWork.persist(info);
    }
  }

  @Test
  public void testFiltering() throws Exception {
    expectItemCount(5);

    FXPlatform.invokeLater(() -> listView.tagContainerController.addTag("tag1"));
    activityController.waitForDataSource();
    expectItemCount(1);
    expectItem(0, "info1");

    FXPlatform.invokeLater(() -> listView.tagContainerController.removeTag("tag1"));
    activityController.waitForDataSource();
    expectItemCount(5);
  }

  private void expectItemCount(int expected) {
    assertEquals(expected, listView.informationList.getItems().size());
  }

  private void expectItem(int index, String name) {
    Information item = listView.informationList.getItems().get(index);
    assertEquals(name, item.getName());
  }
}