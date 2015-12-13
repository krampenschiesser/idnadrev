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
package de.ks.blogging.grav.ui.blog.manage;

import de.ks.blogging.grav.entity.GravBlog;
import de.ks.flatjsondb.PersistentWork;
import de.ks.standbein.datasource.ListDataSource;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;

public class ManageBlogsDS implements ListDataSource<GravBlog> {
  @Inject
  PersistentWork persistentWork;
  @Override
  public List<GravBlog> loadModel(Consumer<List<GravBlog>> furtherProcessing) {
    return persistentWork.from(GravBlog.class);
  }

  @Override
  public void saveModel(List<GravBlog> model, Consumer<List<GravBlog>> beforeSaving) {

  }
}
