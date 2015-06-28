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

import de.ks.blogging.grav.pages.GravPages;
import de.ks.blogging.grav.posts.BasePost;
import de.ks.datasource.DataSource;

import javax.inject.Inject;
import java.util.function.Consumer;

public class CreateEditPostDS implements DataSource<BasePost> {
  private BasePost editPost;
  @Inject
  GravPages gravPages;

  private String commitMsg;

  @Override
  public BasePost loadModel(Consumer<BasePost> furtherProcessing) {
    if (editPost != null) {
      return editPost;
    } else {
      return new UIPostWrapper(gravPages.getBlog().getDateFormat(), gravPages.getBlog().getDefaultAuthor());
    }
  }

  @Override
  public void saveModel(BasePost model, Consumer<BasePost> beforeSaving) {
    beforeSaving.accept(model);

    if (model instanceof UIPostWrapper) {
      UIPostWrapper wrapper = (UIPostWrapper) model;
      PostType postType = wrapper.getPostType();

      BasePost basePost;
      if (postType == PostType.BLOGITEM) {
        basePost = gravPages.addBlogItem(wrapper.getHeader().getTitle());
      } else if (postType == PostType.PAGE) {
        basePost = gravPages.addPage(wrapper.getHeader().getTitle());
      } else {
        basePost = gravPages.addPost(wrapper.getHeader().getTitle(), wrapper.getFilePath());
      }
      basePost.setContent(wrapper.getContent());
      basePost.getHeader().fillFrom(wrapper.getHeader());
      model = basePost;
    }
    model.write();
    if (commitMsg != null) {
      gravPages.addCommit(commitMsg);
    }
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof BasePost) {
      this.editPost = (BasePost) dataSourceHint;
    } else if (dataSourceHint instanceof String) {
      this.commitMsg = (String) dataSourceHint;
    } else {
      editPost = null;
      this.commitMsg = null;
    }
  }
}
