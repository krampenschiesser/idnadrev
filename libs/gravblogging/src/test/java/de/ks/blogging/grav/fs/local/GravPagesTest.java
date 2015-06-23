package de.ks.blogging.grav.fs.local;

import com.google.common.base.StandardSystemProperty;
import de.ks.blogging.grav.GravSettings;
import de.ks.blogging.grav.posts.*;
import de.ks.blogging.grav.posts.media.ImageScalerTest;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.Sanselan;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class GravPagesTest {
  @Test
  public void testDiscoverPosts() throws Exception {
    URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
    String pathname = location.getFile();

    File file = new File(pathname);
    while (!file.getName().equals("gravblogging")) {
      file = file.getParentFile();
    }

    File postDir = new File(file, "src/test/resources/de/ks/blogging/grav/posts/");
    GravPages gravPages = new GravPages(postDir.getAbsolutePath());
    gravPages.scan();
    Collection<BasePost> allPosts = gravPages.getAllPosts();
    assertEquals(3, allPosts.size());

    BasePost blog = gravPages.getAllPosts().stream().filter(p -> p.getFile().getName().equals("blog.md")).findFirst().get();
    assertEquals(1, blog.getMedia().values().size());

    Collection<BlogItem> posts = gravPages.getBlogItems();
    assertEquals(1, posts.size());
    BlogItem post = posts.iterator().next();

    assertEquals("Hello World", post.getHeader().getTitle());
    assertEquals("Hello Sauerland", post.getContent());
    assertEquals(LocalDate.of(2015, 6, 14), post.getHeader().getLocalDate());
    assertEquals(LocalTime.of(14, 3, 0), post.getHeader().getLocalDateTime().get().toLocalTime());
    assertEquals("Christian Loehnert", post.getHeader().getAuthor());
    assertEquals("blog", post.getHeader().getCategory());
    assertEquals(1, post.getHeader().getTags().size());
    assertEquals("hello", post.getHeader().getTags().get(0));

    Collection<Page> pages = gravPages.getPages();
    assertEquals(1, pages.size());
    Page page = pages.iterator().next();

    assertEquals("PCT 2013", page.getHeader().getTitle());
    assertEquals(LocalDate.of(2015, 6, 19), page.getHeader().getLocalDate());
    assertFalse(page.getHeader().getLocalDateTime().isPresent());
    assertEquals("Christian Löhnert", page.getHeader().getAuthor());
    assertEquals("# PCT 2013", page.getContent());
  }

  @Test
  public void testCreatePage() throws Exception {
    Header.GRAV_SETTINGS = () -> new GravSettings();
    GravPages gravPages = new GravPages(StandardSystemProperty.JAVA_IO_TMPDIR.value());

    String title = "Ein Bier, ein Würstchen!#+?  ";
    String content = "Hello Sauerland";

    Page page = gravPages.addPage(title);
    page.getHeader().setLocalDateTime(LocalDateTime.of(2015, 06, 06, 12, 42, 0));
    page.setContent(content);
    page.write();

    File file = page.getFile();
    file.deleteOnExit();
    assertEquals("ein-bier-ein-wuerstchen", file.getParentFile().getName());
    assertEquals("default.md", file.getName());

    List<String> lines = Files.readAllLines(file.toPath());
    assertEquals("title: " + title, lines.get(1));
    assertEquals("author: " + StandardSystemProperty.USER_NAME.value(), lines.get(2));
    assertEquals("date: 06.06.2015 12:42:00", lines.get(3));

    assertEquals(content, lines.get(6));
  }

  @Test
  public void testCreateBlogItem() throws Exception {
    Header.GRAV_SETTINGS = () -> new GravSettings();
    GravPages gravPages = new GravPages(StandardSystemProperty.JAVA_IO_TMPDIR.value());

    String title = "Title 1  ";
    String content = "Bla";

    BlogItem blogItem = gravPages.addBlogItem(title);
    blogItem.setContent(content);
    blogItem.getHeader().setTags("test", "blubber");
    blogItem.write();

    File file = blogItem.getFile();
    file.deleteOnExit();
    assertEquals("01.blog", file.getParentFile().getParentFile().getName());
    assertEquals("title-1", file.getParentFile().getName());
    assertEquals("item.md", file.getName());

    List<String> lines = Files.readAllLines(file.toPath());
    assertEquals("title: " + title, lines.get(1));
    assertEquals("author: " + StandardSystemProperty.USER_NAME.value(), lines.get(2));
    assertEquals(HeaderContainer.INDENTATION + "category: blog", lines.get(4));
    assertEquals(HeaderContainer.INDENTATION + "tag: [test, blubber]", lines.get(5));

    assertEquals(content, lines.get(8));
  }

  @Test
  public void testAddImage() throws Exception {
    Header.GRAV_SETTINGS = () -> new GravSettings();
    GravPages gravPages = new GravPages(StandardSystemProperty.JAVA_IO_TMPDIR.value());
    String title = "Title 1  ";
    String content = "Bla";

    BlogItem blogItem = gravPages.addBlogItem(title);
    blogItem.setContent(content);
    blogItem.getHeader().setTags("test", "blubber");

    File src = new File(ImageScalerTest.class.getResource("landscape.jpg").getFile());
    File mediaFile = blogItem.addMedia(src);

    blogItem.write();
    File file = blogItem.getFile();

    mediaFile.deleteOnExit();
    file.deleteOnExit();

    assertTrue(file.exists());

    assertTrue(mediaFile.exists());
    ImageInfo imageInfo = Sanselan.getImageInfo(mediaFile);
    assertEquals(1024, imageInfo.getWidth());
  }
}