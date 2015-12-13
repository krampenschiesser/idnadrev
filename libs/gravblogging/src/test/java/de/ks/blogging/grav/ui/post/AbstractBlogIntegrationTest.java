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
package de.ks.blogging.grav.ui.post;

import de.ks.blogging.grav.ActivityTest;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.blogging.grav.pages.GravPages;
import de.ks.flatadocdb.session.Session;
import org.junit.After;

import javax.inject.Inject;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractBlogIntegrationTest extends ActivityTest {
  protected final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
  private BlogIntegrationBasicFixture fixture;
  protected File fileBlog1;
  protected File fileBlog2;
  protected LocalDateTime dateTime;

  @Inject
  GravPages pages;

  @After
  public void tearDown() throws Exception {
    pages.close();
    fixture.cleanup();
  }

  @Override
  protected void createTestData(Session session) throws Exception {
    fixture = new BlogIntegrationBasicFixture();
    fixture.createBlogFolders(false);

    fileBlog1 = fixture.fileBlog1;
    fileBlog2 = fixture.fileBlog2;
    dateTime = fixture.dateTime;

    session.persist(new GravBlog("blog1", fixture.fileBlog1.getPath()));
    session.persist(new GravBlog("blog2", fixture.fileBlog2.getPath()));
  }
}
