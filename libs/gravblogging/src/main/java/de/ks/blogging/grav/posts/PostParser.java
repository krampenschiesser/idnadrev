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
package de.ks.blogging.grav.posts;

import de.ks.blogging.grav.entity.GravBlog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

public class PostParser implements Callable<BasePost> {
  private final Path path;
  private final GravBlog blog;

  public PostParser(Path path, GravBlog blog) {
    this.path = path;
    this.blog = blog;
  }

  @Override
  public BasePost call() throws Exception {
    File file = path.toFile();
    String fileName = file.getName();
    List<String> lines = Files.readAllLines(path);

    BasePost post;
    if (fileName.equals("item.md")) {
      post = new BlogItem(file, blog.getDateFormat());
    } else if (fileName.equals("default.md")) {
      post = new Page(file, blog.getDateFormat());
    } else if (fileName.equals("blog.md")) {
      post = new Blog(file, blog.getDateFormat());
    } else {
      post = new BasePost(file, blog.getDateFormat());
    }
    post.getHeader().read(lines);
    post.setContentFromLines(lines.subList(post.getHeader().getBodyStart(), lines.size()));
    post.scanMedia(file.getParentFile());
    return post;
  }
}
