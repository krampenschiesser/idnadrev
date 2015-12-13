package de.ks.blogging.grav.ui.post.manage;

import de.ks.blogging.grav.ui.post.AbstractBlogIntegrationTest;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ManagePostsActivityTest extends AbstractBlogIntegrationTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ManagePostsActivity.class;
  }

  @Test
  public void testSelectBlogAndScan() throws Exception {
    ManagePostsController managePosts = activityController.getControllerInstance(ManagePostsController.class);
    ObservableList items = managePosts.blogSelection.getItems();
    assertEquals(2, items.size());
    assertEquals(0, managePosts.blogSelection.getSelectionModel().getSelectedIndex());

    assertEquals(1, managePosts.postTable.getItems().size());

    FXPlatform.invokeLater(() -> managePosts.blogSelection.getSelectionModel().select(1));
    activityController.waitForDataSource();

    assertEquals(1, managePosts.blogSelection.getSelectionModel().getSelectedIndex());
    assertEquals(2, managePosts.postTable.getItems().size());
  }
}