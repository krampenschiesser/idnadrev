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
package de.ks.blogging.grav.ui.blog.edit;

import com.google.common.base.StandardSystemProperty;
import de.ks.blogging.grav.PostDateFormat;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.flatjsondb.datasource.CreateEditDS;

public class CreateEditBlogDS extends CreateEditDS<GravBlog> {
  public CreateEditBlogDS() {
    super(GravBlog.class);
  }

  @Override
  protected GravBlog getNewInstance() {
    GravBlog newInstance = super.getNewInstance();
    newInstance.setDefaultAuthor(StandardSystemProperty.USER_NAME.value());
    newInstance.setImageDimension(1024);
    newInstance.setDateFormat(PostDateFormat.EUROPEAN);
    return newInstance;
  }
}
