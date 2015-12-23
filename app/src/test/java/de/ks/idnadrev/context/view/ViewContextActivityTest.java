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

package de.ks.idnadrev.context.view;

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Task;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class ViewContextActivityTest extends ActivityTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());

  private ViewContextController controller;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ViewContextActivity.class;
  }

  @Override
  protected void createTestData(Session session) {
    session.persist(new Context("context1"));
    Context context2 = new Context("context2");
    session.persist(context2);
    session.persist(new Task("test").setContext(context2));
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(ViewContextController.class);
  }

  @Test
  public void testEmpty() throws Exception {
    persistentWork.removeAllOf(Context.class);

    activityController.reload();
    activityController.waitForDataSource();

    assertTrue(controller.edit.isDisabled());
    assertTrue(controller.delete.isDisabled());
    assertFalse(controller.create.isDisabled());
  }

  @Test
  public void testDelete() throws Exception {
    assertEquals(2, controller.contextList.getItems().size());
    FXPlatform.invokeLater(() -> controller.contextList.getSelectionModel().select(1));
    FXPlatform.waitForFX();
    assertEquals("context2", controller.contextList.getSelectionModel().getSelectedItem().getName());

    FXPlatform.invokeLater(() -> controller.onDelete());
    activityController.waitForDataSource();
    persistentWork.run(session -> {
      Task task = persistentWork.from(Task.class).get(0);
      assertNull(task.getContext());
      assertEquals(1, persistentWork.from(Context.class).size());
    });
  }
}