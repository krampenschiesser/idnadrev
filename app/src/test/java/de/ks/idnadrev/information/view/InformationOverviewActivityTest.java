package de.ks.idnadrev.information.view;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Category;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class InformationOverviewActivityTest extends ActivityTest {

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
  protected void createTestData(EntityManager em) {
    for (int i = 0; i < 5; i++) {
      Tag tag = new Tag("tag" + i);
      Category category = new Category("category" + i);
      TextInfo info = new TextInfo("info" + i);
      info.addTag(tag);
      info.setCategory(category);
      PersistentWork.persist(tag, category, info);
    }
  }

  @Test
  public void testFiltering() throws Exception {
    assertEquals(5, listView.informationList.getItems().size());

    listView.tagContainerController.addTag("tag1");
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

  }
}