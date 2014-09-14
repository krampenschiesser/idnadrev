/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.idnadrev.tag;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Tag;
import de.ks.util.FXPlatform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class TagContainerTest extends ActivityTest {
  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return TestTagActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    em.persist(new Tag("tag1"));
    em.persist(new Tag("tag2"));
  }

  @Test
  public void testAddTag() throws Exception {
    TagContainer container = activityController.getControllerInstance(TagContainer.class);
    assertEquals(0, container.tagPane.getChildren().size());

    addTag(container, "tag1");
    assertEquals(0, container.tagAddController.getInput().getText().length());
    assertEquals(1, container.tagPane.getChildren().size());

    addTag(container, "tag1");
    assertEquals(1, container.tagPane.getChildren().size());

    addTag(container, "tag2");
    assertEquals(2, container.tagPane.getChildren().size());

    activityController.save();
    activityController.waitForDataSource();

    TestTagDS ds = (TestTagDS) store.getDatasource();
    assertNotNull(ds.saved);
    assertEquals(2, ds.saved.getTags().size());
  }

  @Test
  public void testAddRemove() throws Exception {
    TagContainer container = activityController.getControllerInstance(TagContainer.class);

    FXPlatform.invokeLater(() -> container.addTag("bla"));
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals(1, container.tagPane.getChildren().size());

    FXPlatform.invokeLater(() -> container.removeTag("bla"));
    assertEquals(0, container.tagPane.getChildren().size());

    addTag(container, "tag1");

    FXPlatform.invokeLater(() -> {
      Node node = container.tagPane.getChildren().get(0);
      Button lookup = (Button) node.lookup("#remove");
      lookup.getOnAction().handle(null);
    });
    assertEquals(0, container.tagPane.getChildren().size());
  }

  private void addTag(TagContainer container, String tagName) throws Exception {
    FXPlatform.invokeLater(() -> {
      container.tagAddController.getInput().setText(tagName);
      container.tagAddController.getOnAction().handle(null);
    });

    activityController.waitForTasks();
    FXPlatform.waitForFX();
  }
}