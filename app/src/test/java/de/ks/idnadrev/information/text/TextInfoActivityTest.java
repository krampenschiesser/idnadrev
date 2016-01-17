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

package de.ks.idnadrev.information.text;

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.Information;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TextInfoActivityTest extends ActivityTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return TextInfoActivity.class;
  }

  @Before
  public void setUp() throws Exception {
    TextInfoController controller = activityController.getControllerInstance(TextInfoController.class);
//    FXPlatform.invokeLater(() -> controller.content.getPersistentStoreBack().delete());
  }

  @Override
  protected void createTestData(Session session) {
  }

  @Test
  public void testCreateNew() throws Exception {
    TextInfoController controller = activityController.getControllerInstance(TextInfoController.class);
    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      controller.content.setText("= title\n\nbla");
      controller.tagContainerController.addTag("tag1");
    });
    FXPlatform.waitForFX();
    activityController.waitForTasks();
    activityController.save();
    activityController.waitForDataSource();

    persistentWork.run(session -> {
      Information textInfo = persistentWork.forName(Information.class, "test");
      assertNotNull(textInfo);


      Set<Tag> tags = textInfo.getTags();
      assertEquals(1, tags.size());
      Tag tag = tags.iterator().next();
      assertEquals("tag1", tag.getDisplayName());
      assertEquals("= title\n\nbla", textInfo.getContent());
    });
  }

  @Test
  public void testEdit() throws Exception {
    TextInfoDS datasource = (TextInfoDS) store.getDatasource();
    Information model = persistentWork.read(em -> {
      Information textInfo = new Information("test").setContent("desc");
      textInfo.addTag("tag");
      em.persist(textInfo);
      return textInfo;
    });
    datasource.setLoadingHint(model);

    activityController.reload();
    activityController.waitForDataSource();

    TextInfoController controller = activityController.getControllerInstance(TextInfoController.class);
    assertEquals("test", controller.name.getText());
    assertEquals("desc", controller.content.getText());
    assertTrue(controller.tagContainerController.getCurrentTags().contains("tag"));

    FXPlatform.invokeLater(() -> controller.content.setText("other"));
    activityController.save();
    activityController.waitForDataSource();

    persistentWork.run(session -> {
      List<Information> from = persistentWork.from(Information.class);
      assertEquals(1, from.size());
      Information info = from.get(0);
      assertEquals("other", info.getContent());
    });
  }
}