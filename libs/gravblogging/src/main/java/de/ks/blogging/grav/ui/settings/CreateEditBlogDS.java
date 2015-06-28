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
package de.ks.blogging.grav.ui.settings;

import de.ks.blogging.grav.entity.GravBlog;
import de.ks.datasource.DataSource;

import java.util.function.Consumer;

public class CreateEditBlogDS implements DataSource<GravBlog> {
  @Override
  public GravBlog loadModel(Consumer<GravBlog> furtherProcessing) {
    return null;
  }

  @Override
  public void saveModel(GravBlog model, Consumer<GravBlog> beforeSaving) {
    beforeSaving.accept(model);
  }
}
