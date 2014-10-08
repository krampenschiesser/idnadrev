package de.ks.idnadrev.information.view;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.executor.group.LastTextChange;
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
    expectItemCount(5);

    FXPlatform.invokeLater(() -> listView.tagContainerController.addTag("tag1"));
    activityController.waitForDataSource();
    expectItemCount(1);
    expectItem(0, "info1");

    FXPlatform.invokeLater(() -> listView.tagContainerController.removeTag("tag1"));
    activityController.waitForDataSource();
    expectItemCount(5);

    FXPlatform.invokeLater(() -> listView.categorySelectionController.getInput().setText("category3"));
    Thread.sleep(LastTextChange.WAIT_TIME + 100);
    activityController.waitForDataSource();
    expectItemCount(1);
    expectItem(0, "info3");

    FXPlatform.invokeLater(() -> listView.categorySelectionController.getInput().setText(""));
    FXPlatform.invokeLater(() -> listView.nameSearch.setText("4"));
    Thread.sleep(LastTextChange.WAIT_TIME + 100);
    activityController.waitForDataSource();
    expectItemCount(1);
    expectItem(0, "info4");
  }

  private void expectItemCount(int expected) {
    assertEquals(expected, listView.informationList.getItems().size());
  }

  private void expectItem(int index, String name) {
    InformationPreviewItem item = listView.informationList.getItems().get(index);
    assertEquals(name, item.getName());
  }
}