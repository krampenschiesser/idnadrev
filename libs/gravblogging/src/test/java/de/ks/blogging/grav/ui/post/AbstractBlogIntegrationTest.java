/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.ui.post;

import com.google.common.base.StandardSystemProperty;
import de.ks.blogging.grav.ActivityTest;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.pages.GravPages;
import org.junit.After;

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

public abstract class AbstractBlogIntegrationTest extends ActivityTest {
  protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
  protected File fileBlog1;
  protected File fileBlog2;
  protected LocalDateTime dateTime;

  public void createBlogFolders() throws Exception {
    dateTime = LocalDateTime.now().withSecond(0).withNano(0);
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

  // utility methods

  protected String getBlog(String title, String content) {
    StringBuilder builder = new StringBuilder();
    builder.append("---\n");
    builder.append("title: ").append(title).append("\n");
    builder.append("date: ").append(dateTimeFormatter.format(dateTime)).append("\n");
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
