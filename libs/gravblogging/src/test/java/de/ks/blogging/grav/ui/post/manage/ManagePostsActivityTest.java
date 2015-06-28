package de.ks.blogging.grav.ui.post.manage;

import com.google.common.base.StandardSystemProperty;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.blogging.grav.ActivityTest;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.pages.GravPages;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ManagePostsActivityTest extends ActivityTest {
  protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
  private File fileBlog1;
  private File fileBlog2;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ManagePostsActivity.class;
  }

  public void createBlogFolders() throws Exception {
    String tmpDir = StandardSystemProperty.JAVA_IO_TMPDIR.value();
    fileBlog1 = new File(tmpDir, "blog1");
    fileBlog2 = new File(tmpDir, "blog2");
    fileBlog1.mkdir();
    fileBlog2.mkdir();

    Files.write(new File(fileBlog1, "blog1.md").toPath(), Arrays.asList(getBlog("post 1", "Hello Sauerland")));
    Files.write(new File(fileBlog2, "blog2_a.md").toPath(), Arrays.asList(getBlog("post 1", "Hello Woll")));
    Files.write(new File(fileBlog2, "blog2_b.md").toPath(), Arrays.asList(getBlog("post 2", "Ein Bier bitte")));
  }

  @After
  public void tearDown() throws Exception {
    CDI.current().select(GravPages.class).get().close();
    deleteDir(fileBlog1);
    deleteDir(fileBlog2);
  }

  @Override
  protected void createTestData(EntityManager em) throws Exception {
    createBlogFolders();
    em.persist(new GravBlog("blog1", fileBlog1.getPath()));
    em.persist(new GravBlog("blog2", fileBlog2.getPath()));
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


  // utility methods

  protected String getBlog(String title, String content) {
    StringBuilder builder = new StringBuilder();
    builder.append("---\n");
    builder.append("title: ").append(title).append("\n");
    builder.append("date: ").append(dateTimeFormatter.format(LocalDateTime.now())).append("\n");
    builder.append("---\n");
    builder.append(content).append("\n");
    return builder.toString();
  }

  private void deleteDir(File dir) throws IOException {
    SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.deleteIfExists(file);
        return super.visitFile(file, attrs);
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.deleteIfExists(dir);
        return super.postVisitDirectory(dir, exc);
      }
    };
    Files.walkFileTree(dir.toPath(), visitor);
    Files.deleteIfExists(dir.toPath());
  }
}