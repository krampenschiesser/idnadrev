/**
 * Copyright [2015] [Christian Loehnert]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.blogging.grav.pages;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.posts.BasePost;
import de.ks.blogging.grav.posts.BlogItem;
import de.ks.blogging.grav.posts.Page;
import de.ks.blogging.grav.posts.PostParser;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Default;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Singleton
@Default
public class GravPages implements AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(GravPages.class);

  protected GravBlog blog;
  protected final Set<BasePost> posts = new HashSet<>();
  protected final Set<Future<BasePost>> postLoader = new HashSet<>();
  protected final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());
  protected final AtomicReference<Git> git = new AtomicReference<>();

  public GravPages() {
    this(null);
  }
  public GravPages(GravBlog blog) {
    this.blog = blog;
  }

  public GravBlog getBlog() {
    return blog;
  }

  public void setBlog(GravBlog blog) {
    this.blog = blog;
  }

  public synchronized GravPages scan() {
    String filePath = blog.getPagesDirectory();
    posts.clear();
    postLoader.clear();
    SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file != null && file.toString().endsWith("md")) {
          postLoader.add(executorService.submit(new PostParser(file, blog)));
        }
        return FileVisitResult.CONTINUE;
      }
    };
    try {
      Files.walkFileTree(Paths.get(filePath), visitor);
    } catch (IOException e) {
      log.error("Could not walk fileTree {}", filePath, e);
    }
    return this;
  }

  public synchronized Set<BasePost> getPosts() {
    if (!postLoader.isEmpty()) {
      postLoader.forEach(f -> {
        try {
          BasePost loaded = f.get();
          posts.add(loaded);
        } catch (Exception e) {
          log.error("Could not read post ", e);
        }
      });
      postLoader.clear();
    }
    return posts;
  }

  public synchronized BasePost addPost(String title, String fileName) {
    File file = getPostFile(title, fileName);
    BasePost page = new BasePost(file, blog.getDateFormat());
    page.getHeader().setTitle(title);
    page.getHeader().setAuthor(blog.getDefaultAuthor());
    return page;
  }

  public synchronized Page addPage(String title, int index) {
    String fileName = "default.md";
    File file = getPostFile(index + "." + title, fileName);
    Page page = new Page(file, blog.getDateFormat());
    page.getHeader().setTitle(title);
    page.getHeader().setAuthor(blog.getDefaultAuthor());
    return page;
  }

  public synchronized BlogItem addBlogItem(String title) {
    String fileName = "item.md";
    String blogSubPath = blog.getBlogSubPath();
    File parent = new File(new File(blog.getPagesDirectory()), blogSubPath);
    File file = getPostFile(title, fileName, parent);

    BlogItem page = new BlogItem(file, blog.getDateFormat());
    page.getHeader().setTitle(title);
    page.getHeader().setCategory("blog");
    page.getHeader().setAuthor(blog.getDefaultAuthor());
    return page;
  }

  protected File getPostFile(String title, String fileName) {
    return getPostFile(title, fileName, new File(blog.getPagesDirectory()));
  }

  protected File getPostFile(String title, String fileName, File parent) {
    StringBuilder folderName = new StringBuilder();
    char[] chars = StringUtils.replace(title.toLowerCase(Locale.ROOT).trim(), " ", "-").toCharArray();
    Charset ascii = Charsets.US_ASCII;
    CharsetEncoder encoder = ascii.newEncoder();
    for (char character : chars) {
      boolean isCharacter = Character.isLetterOrDigit(character) || character == '-' || character == '.';
      boolean isAscii = encoder.canEncode(character);
      if (isCharacter && isAscii) {
        folderName.append(character);
      } else if (character == '\u00e4') {
        folderName.append("ae");
      } else if (character == '\u00f6') {
        folderName.append("oe");
      } else if (character == '\u00fc') {
        folderName.append("ue");
      }
    }

    File folder = new File(parent, folderName.toString());
    return new File(folder, fileName);
  }

  public Collection<Page> getPages() {
    return getPosts().stream().filter(p -> (p instanceof Page)).map(p -> (Page) p).collect(Collectors.toList());
  }

  public Collection<BlogItem> getBlogItems() {
    return getPosts().stream().filter(p -> (p instanceof BlogItem)).map(p -> (BlogItem) p).collect(Collectors.toList());
  }

  public Collection<BasePost> getAllPosts() {
    return getPosts();
  }

  public boolean hasGitRepository() {
    return new RepositoryBuilder().findGitDir(new File(blog.getPagesDirectory())).setMustExist(true).getGitDir() != null;
  }

  public Git getGit() {
    git.getAndUpdate(git -> {
      if (git == null) {
        try {
          Repository repository = new RepositoryBuilder().findGitDir(new File(blog.getPagesDirectory())).setMustExist(true).build();
          return new Git(repository);
        } catch (Exception e) {
          log.error("Could not get Git ", e);
          return null;
        }
      } else {
        return git;
      }
    });
    return git.get();
  }

  public void addCommit(String msg) throws RuntimeException {
    Git git = getGit();
    if (git != null) {
      try {
        Status status = git.status().call();

        Set<String> modified = status.getModified();
        Set<String> untracked = status.getUntracked();

        AddCommand add = git.add();
        modified.forEach(s -> add.addFilepattern(s));
        untracked.forEach(s -> add.addFilepattern(s));
        add.call();


        CommitCommand commit = git.commit();
        if (msg == null || msg.isEmpty()) {
          commit.setAmend(true);
        } else {
          commit.setMessage(msg);
        }
        RevCommit rev = commit.call();
        log.info("Commited change {} with new rev {}", msg, rev);
      } catch (Exception e) {
        log.error("Could not add and commit ", e);
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void close() throws Exception {
    if (git.get() != null) {
      git.get().close();
    }
  }
}
