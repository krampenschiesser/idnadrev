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
package de.ks.blogging.grav.ui.post.manage;

import de.ks.blogging.grav.fs.local.GravPages;
import de.ks.blogging.grav.posts.BasePost;
import de.ks.datasource.ListDataSource;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ManagePostsDS implements ListDataSource<BasePost> {

  @Inject
  GravPages pages;

  @Override
  public List<BasePost> loadModel(Consumer<List<BasePost>> furtherProcessing) {
    Comparator<BasePost> comparing = Comparator.comparing(p -> p.getHeader().getLocalDateTime().orElse(LocalDateTime.now()));
    List<BasePost> allPosts = pages.getAllPosts().stream()//
      .filter(p -> p != null)//
      .filter(p -> p.getHeader().getTitle() != null && p.getHeader().getTitle().length() > 0)//
      .sorted(comparing.reversed())//
      .collect(Collectors.toList());
    return allPosts;
  }

  @Override
  public void saveModel(List<BasePost> model, Consumer<List<BasePost>> beforeSaving) {

  }
}
