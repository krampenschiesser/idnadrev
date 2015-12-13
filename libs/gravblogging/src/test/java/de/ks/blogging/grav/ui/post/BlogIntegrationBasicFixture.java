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
import de.ks.standbein.FileUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class BlogIntegrationBasicFixture {
  protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
  protected File fileBlog1;
  protected File fileBlog2;
  protected LocalDateTime dateTime;
  private String commit1;
  private String commit2;
  private String commit3;
  private String commitMoved;
  private String commitDeleted;

  public void createBlogFolders(boolean withGit) throws Exception {
    dateTime = LocalDateTime.now().withSecond(0).withNano(0);
    String tmpDir = StandardSystemProperty.JAVA_IO_TMPDIR.value();
    fileBlog1 = new File(tmpDir, "blog1");
    fileBlog2 = new File(tmpDir, "blog2");
    fileBlog1.mkdir();
    fileBlog2.mkdir();

    Git git = null;
    if (withGit) {
      git = Git.init().setDirectory(fileBlog2).call();
    }

    Files.write(new File(fileBlog1, "blog1.md").toPath(), Arrays.asList(getBlog("post 1", "Hello Sauerland")));
    Files.write(new File(fileBlog2, "blog2_a.md").toPath(), Arrays.asList(getBlog("post 1", "Hello Woll")));
    if (git != null) {
      git.add().addFilepattern("blog2_a.md").call();
      RevCommit commit = git.commit().setAll(true).setMessage("commit 1").call();
      commit1 = commit.getId().getName();
    }
    Files.write(new File(fileBlog2, "blog2_b.md").toPath(), Arrays.asList(getBlog("post 2", "Ein Bier bitte")));
    if (git != null) {
      git.add().addFilepattern("blog2_b.md").call();
      RevCommit commit = git.commit().setAll(true).setMessage("commit 2").call();
      commit2 = commit.getId().getName();
    }
    if (git != null) {
      Files.write(new File(fileBlog2, "blog2_c.md").toPath(), Arrays.asList(getBlog("post 3", "Ein Tischgedeck bitte")));

      git.add().addFilepattern("blog2_c.md").call();
      RevCommit commit = git.commit().setAll(true).setMessage("commit 3").call();
      commit3 = commit.getId().getName();

      Files.move(new File(fileBlog2, "blog2_c.md").toPath(), new File(fileBlog2, "blog2_d.md").toPath());

      git.add().addFilepattern("blog2_c.md").addFilepattern("blog2_d.md").call();
      commit = git.commit().setAll(true).setMessage("commit moved").call();
      commitMoved = commit.getId().getName();

      Files.delete(new File(fileBlog2, "blog2_d.md").toPath());

      git.add().addFilepattern("blog2_d.md").call();
      commit = git.commit().setAll(true).setMessage("commit deleted").call();
      commitDeleted = commit.getId().getName();
    }
  }

  public void cleanup() throws IOException {
    FileUtil.deleteDir(fileBlog1);
    FileUtil.deleteDir(fileBlog2);
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

  public File getBlog2() {
    return fileBlog2;
  }

  public File getGitBlog() {
    return fileBlog2;
  }

  public File getBlog1() {
    return fileBlog1;
  }

  public String getCommit1() {
    return commit1;
  }

  public String getCommit2() {
    return commit2;
  }

  public String getCommit3() {
    return commit3;
  }

  public String getCommitMoved() {
    return commitMoved;
  }

  public String getCommitDeleted() {
    return commitDeleted;
  }
}
