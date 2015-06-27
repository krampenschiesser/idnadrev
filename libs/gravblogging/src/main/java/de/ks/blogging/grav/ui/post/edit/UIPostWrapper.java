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
package de.ks.blogging.grav.ui.post.edit;

import de.ks.blogging.grav.posts.BasePost;
import de.ks.blogging.grav.posts.Header;

import java.time.LocalDateTime;

public class UIPostWrapper extends BasePost {
  protected PostType postType;
  protected String filePath;
  protected Integer pageIndex;

  public UIPostWrapper() {
    super(null);
    getHeader().setLocalDateTime(LocalDateTime.now());
    getHeader().setPublished(true);
    getHeader().setAuthor(Header.GRAV_SETTINGS.get().getDefaultAuthor());
  }

  public PostType getPostType() {
    return postType;
  }

  public void setPostType(PostType postType) {
    this.postType = postType;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public Integer getPageIndex() {
    return pageIndex;
  }

  public void setPageIndex(Integer pageIndex) {
    this.pageIndex = pageIndex;
  }
}
