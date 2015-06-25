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
package de.ks.blogging.grav.fs.local;

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.blogging.grav.GravSettings;
import de.ks.blogging.grav.posts.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;

public class GravPages {
  private static final Logger log = LoggerFactory.getLogger(GravPages.class);

  protected final String filePath;
  protected final Set<BasePost> posts = new HashSet<>();
  protected final Set<Future<BasePost>> postLoader = new HashSet<>();
  protected final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

  public GravPages(String filePath) {
    this.filePath = filePath;
  }

  public synchronized GravPages scan() {
    SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file != null && file.toString().endsWith("md")) {
          postLoader.add(executorService.submit(new PostParser(file)));
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
    BasePost page = new BasePost(file);
    page.getHeader().setTitle(title);
    page.getHeader().setAuthor(Header.GRAV_SETTINGS.get().getDefaultAuthor());
    return page;
  }

  public synchronized Page addPage(String title) {
    String fileName = "default.md";
    File file = getPostFile(title, fileName);
    Page page = new Page(file);
    page.getHeader().setTitle(title);
    page.getHeader().setAuthor(Header.GRAV_SETTINGS.get().getDefaultAuthor());
    return page;
  }

  public synchronized BlogItem addBlogItem(String title) {
    GravSettings gravSettings = Header.GRAV_SETTINGS.get();

    String fileName = "item.md";
    String blogSubPath = gravSettings.getBlogSubPath();
    File parent = new File(new File(filePath), blogSubPath);
    File file = getPostFile(title, fileName, parent);

    BlogItem page = new BlogItem(file);
    page.getHeader().setTitle(title);
    page.getHeader().setCategory("blog");
    page.getHeader().setAuthor(gravSettings.getDefaultAuthor());
    return page;
  }

  protected File getPostFile(String title, String fileName) {
    return getPostFile(title, fileName, new File(filePath));
  }

  protected File getPostFile(String title, String fileName, File parent) {
    StringBuilder folderName = new StringBuilder();
    char[] chars = StringUtils.replace(title.toLowerCase(Locale.ROOT).trim(), " ", "-").toCharArray();
    Charset ascii = Charsets.US_ASCII;
    CharsetEncoder encoder = ascii.newEncoder();
    for (char character : chars) {
      boolean isCharacter = Character.isLetterOrDigit(character) || character == '-';
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
}