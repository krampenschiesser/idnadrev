package de.ks.blogging.grav.ui.post.edit;

import com.google.common.base.StandardSystemProperty;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.pages.GravPages;
import de.ks.blogging.grav.posts.BasePost;
import de.ks.blogging.grav.ui.post.AbstractBlogIntegrationTest;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class CreateEditPostActivityTest extends AbstractBlogIntegrationTest {

  @Inject
  GravPages gravPages;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return CreateEditPostActivity.class;
  }

  @Override
  protected void beforeActivityStart() throws Exception {
    GravBlog blog1 = PersistentWork.forName(GravBlog.class, "blog1");
    gravPages.setBlog(blog1);
    gravPages.scan();
  }

  @Test
  public void testEditPost() throws Exception {
    store.getDatasource().setLoadingHint(gravPages.getAllPosts().iterator().next());
    store.reload();
    store.waitForDataSource();

    CreateEditPostController editor = activityController.getControllerInstance(CreateEditPostController.class);
    assertEquals("post 1", editor.title.getText());
    assertEquals("Hello Sauerland", editor.editor.getText().trim());
    assertEquals(dateTime.toLocalDate(), editor.date.getValue());
    FXPlatform.invokeLater(() -> editor.validator.apply(editor.time, editor.time.getText()));
    assertEquals(dateTime.toLocalTime(), editor.validator.getTime());

    LocalDate yesterday = LocalDate.now().minusDays(1);
    FXPlatform.invokeLater(() -> {
      editor.date.setValue(yesterday);
      editor.time.setText("12:33");
      editor.title.setText("post 1 edited");
      editor.tags.setText("tag 1, tag 2");
      editor.editor.setText("CDT here I come");
    });
    activityController.save();
    activityController.waitForDataSource();

    gravPages.scan();
    BasePost post = gravPages.getAllPosts().iterator().next();

    assertEquals("post 1 edited", post.getHeader().getTitle());
    assertEquals("CDT here I come", post.getContent());

    LocalDateTime expectedTime = LocalDateTime.of(yesterday, LocalTime.of(12, 33));
    assertEquals(expectedTime, post.getHeader().getLocalDateTime().get());
    assertEquals(2, post.getHeader().getTags().size());
    assertEquals("tag 1", post.getHeader().getTags().get(0));
    assertEquals("tag 2", post.getHeader().getTags().get(1));
  }

  @Test
  public void testCreateBlogItem() throws Exception {
    CreateEditPostController editor = activityController.getControllerInstance(CreateEditPostController.class);

    LocalDate today = LocalDate.now();
    FXPlatform.invokeLater(() -> {
      editor.type.setValue(PostType.BLOGITEM);
      editor.date.setValue(today);
      editor.time.setText("12:33");
      editor.title.setText("post 2");
      editor.tags.setText("tag 1, tag 2");
      editor.editor.setText("CDT here I come");
    });
    activityController.save();
    activityController.waitForDataSource();

    gravPages.scan();
    assertEquals(2, gravPages.getAllPosts().size());
    assertEquals(1, gravPages.getBlogItems().size());

    BasePost post = gravPages.getBlogItems().iterator().next();

    assertEquals("post 2", post.getHeader().getTitle());
    assertEquals("CDT here I come", post.getContent());

    LocalDateTime expectedTime = LocalDateTime.of(today, LocalTime.of(12, 33));
    assertEquals(expectedTime, post.getHeader().getLocalDateTime().get());
    assertEquals(2, post.getHeader().getTags().size());
    assertEquals("tag 1", post.getHeader().getTags().get(0));
    assertEquals("tag 2", post.getHeader().getTags().get(1));
  }

  @Test
  public void testCreatePage() throws Exception {
    CreateEditPostController editor = activityController.getControllerInstance(CreateEditPostController.class);

    LocalDate today = LocalDate.now();
    FXPlatform.invokeLater(() -> {
      editor.type.setValue(PostType.PAGE);
      editor.date.setValue(today);
      editor.time.setText("12:33");
      editor.title.setText("post 2");
      editor.tags.setText("tag 1, tag 2");
      editor.pageIndex.setText("42");
      editor.editor.setText("CDT here I come");
    });
    activityController.save();
    activityController.waitForDataSource();

    gravPages.scan();
    assertEquals(2, gravPages.getAllPosts().size());
    assertEquals(1, gravPages.getPages().size());

    BasePost post = gravPages.getPages().iterator().next();

    assertEquals("post 2", post.getHeader().getTitle());
    assertEquals("CDT here I come", post.getContent());

    LocalDateTime expectedTime = LocalDateTime.of(today, LocalTime.of(12, 33));
    assertEquals(expectedTime, post.getHeader().getLocalDateTime().get());
    assertEquals(2, post.getHeader().getTags().size());
    assertEquals("tag 1", post.getHeader().getTags().get(0));
    assertEquals("tag 2", post.getHeader().getTags().get(1));
  }

  @Test
  public void testCreateUnknown() throws Exception {
    CreateEditPostController editor = activityController.getControllerInstance(CreateEditPostController.class);

    LocalDate today = LocalDate.now();
    FXPlatform.invokeLater(() -> {
      editor.type.setValue(PostType.UNKNOWN);
      editor.date.setValue(today);
      editor.time.setText("12:33");
      editor.title.setText("post 2");
      editor.tags.setText("tag 1, tag 2");
      editor.editor.setText("CDT here I come");
      File path = new File(fileBlog1, "other" + StandardSystemProperty.FILE_SEPARATOR + "myitem.md");
      editor.filePath.setText(path.getPath());
    });
    activityController.save();
    activityController.waitForDataSource();

    gravPages.scan();
    assertEquals(2, gravPages.getAllPosts().size());
    assertEquals(0, gravPages.getBlogItems().size());
    assertEquals(0, gravPages.getPages().size());

    BasePost post = gravPages.getAllPosts().stream().filter(s -> s.getHeader().getTitle().equals("post 2")).findFirst().get();

    assertEquals("post 2", post.getHeader().getTitle());
    assertEquals("CDT here I come", post.getContent());

    LocalDateTime expectedTime = LocalDateTime.of(today, LocalTime.of(12, 33));
    assertEquals(expectedTime, post.getHeader().getLocalDateTime().get());
    assertEquals(2, post.getHeader().getTags().size());
    assertEquals("tag 1", post.getHeader().getTags().get(0));
    assertEquals("tag 2", post.getHeader().getTags().get(1));
  }
}