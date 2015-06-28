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
package de.ks.blogging.grav.entity;

import com.google.common.base.StandardSystemProperty;
import de.ks.blogging.grav.PostDateFormat;
import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class GravBlog extends NamedPersistentObject<GravBlog> {
  private static final long serialVersionUID = 1L;
  protected String pagesDirectory;
  protected String blogSubPath;

  protected String defaultAuthor;
  protected int imageDimension;
  @Enumerated(EnumType.STRING)
  protected PostDateFormat dateFormat;

  protected GravBlog() {
    //hibernate
  }

  public GravBlog(String name, String pagesDirectory) {
    super(name);
    this.pagesDirectory = pagesDirectory;
    blogSubPath = "01.blog";
    defaultAuthor = StandardSystemProperty.USER_NAME.value();
    imageDimension = 1024;
    dateFormat = PostDateFormat.EUROPEAN;
  }

  public String getPagesDirectory() {
    return pagesDirectory;
  }

  public GravBlog setPagesDirectory(String pagesDirectory) {
    this.pagesDirectory = pagesDirectory;
    return this;
  }

  public String getBlogSubPath() {
    return blogSubPath;
  }

  public GravBlog setBlogSubPath(String blogSubPath) {
    this.blogSubPath = blogSubPath;
    return this;
  }

  public String getDefaultAuthor() {
    return defaultAuthor;
  }

  public GravBlog setDefaultAuthor(String defaultAuthor) {
    this.defaultAuthor = defaultAuthor;
    return this;
  }

  public int getImageDimension() {
    return imageDimension;
  }

  public GravBlog setImageDimension(int imageDimension) {
    this.imageDimension = imageDimension;
    return this;
  }

  public PostDateFormat getDateFormat() {
    return dateFormat;
  }

  public GravBlog setDateFormat(PostDateFormat dateFormat) {
    this.dateFormat = dateFormat;
    return this;
  }
}
