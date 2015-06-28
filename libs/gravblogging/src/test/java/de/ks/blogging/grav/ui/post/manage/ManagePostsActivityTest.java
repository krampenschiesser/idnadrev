package de.ks.blogging.grav.ui.post.manage;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.blogging.grav.ui.post.AbstractBlogIntegrationTest;
import de.ks.blogging.grav.ui.post.edit.CreateEditPostActivity;
import de.ks.blogging.grav.ui.post.edit.CreateEditPostController;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ManagePostsActivityTest extends AbstractBlogIntegrationTest {

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

  @Test
  public void testEditPost() throws Exception {
    ManagePostsController managePosts = activityController.getControllerInstance(ManagePostsController.class);
    managePosts.onEdit();

    activityController.waitForTasks();
    assertEquals(CreateEditPostActivity.class.getSimpleName(), activityController.getCurrentActivityId());

    CreateEditPostController editor = activityController.getControllerInstance(CreateEditPostController.class);
    assertEquals("post 1", editor);

  }

}