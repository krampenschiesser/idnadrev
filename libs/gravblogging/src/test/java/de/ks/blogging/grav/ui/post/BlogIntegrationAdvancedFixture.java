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
import de.ks.flatadocdb.util.DeleteDir;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlogIntegrationAdvancedFixture {
  static interface RunnableWithException {
    void run() throws Exception;
  }

  protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
  protected File blogFolder;
  protected LocalDateTime dateTime;
  private File pagesDir;
  private Git git;
  protected final List<String> commits = new ArrayList<>();

  public void createBlogFolders() throws Exception {
    cleanup();
    dateTime = LocalDateTime.now().withSecond(0).withNano(0);
    String tmpDir = StandardSystemProperty.JAVA_IO_TMPDIR.value();
    blogFolder = new File(tmpDir, "blog");
    blogFolder.mkdir();

    git = Git.init().setDirectory(blogFolder).call();
    Path dir = Paths.get(blogFolder.getAbsolutePath(), "user", "pages", "01.blog");
    Files.createDirectories(dir);
    this.pagesDir = dir.toFile();

    withGit("entry1", () -> {
      makeBlogEntry("entry1", "post 1", "Hello Sauerland");
    });
    withGit("entry2", () -> {
      makeBlogEntry("entry2", "post 2", "Hello World");
    });
    withGit("modify entry2", () -> {
      modifyBlogEntry("entry2", "post 2", "Hello PCT");
    });
    withGit("entry3", () -> {
      makeBlogEntry("entry3", "post 3", "Hungry!");
    });
    withGit("delete entry 1", () -> {
      new DeleteDir(new File(pagesDir, "entry1")).delete();
    });

    git.close();
  }

  protected void makeBlogEntry(String folderName, String title, String content) throws IOException {
    File parent = new File(pagesDir, folderName);
    parent.mkdir();
    File file = new File(parent, "item.md");

    Files.write(file.toPath(), Arrays.asList(getBlog(title, content)));
  }

  protected void modifyBlogEntry(String folderName, String title, String content) throws IOException {
    File parent = new File(pagesDir, folderName);
    File file = new File(parent, "item.md");
    Files.write(file.toPath(), Arrays.asList(getBlog(title, content)));
  }

  private void withGit(String msg, RunnableWithException r) throws Exception {
    r.run();
    git.add().addFilepattern(".").addFilepattern(blogFolder.getAbsolutePath()).addFilepattern(".*").addFilepattern("*").call();
    String name = git.commit().setMessage(msg).setAll(true).call().getName();
    commits.add(name);
  }

  public void cleanup() throws IOException {
    new DeleteDir(blogFolder).delete();
  }

  protected String getBlog(String title, String content) {
    StringBuilder builder = new StringBuilder();
    builder.append("---\n");
    builder.append("title: ").append(title).append("\n");
    builder.append("date: ").append(dateTimeFormatter.format(dateTime)).append("\n");
    builder.append("---\n");
    builder.append(content).append("\n");
    return builder.toString();
  }

  public List<String> getCommits() {
    return commits;
  }

  public File getBlogFolder() {
    return blogFolder;
  }

  public File getPagesDir() {
    return pagesDir;
  }
}
