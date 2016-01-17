/*
 * Copyright [2016] [Christian Loehnert]
 *
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

package de.ks.idnadrev.context.create;

import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.idnadrev.entity.Context;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CreateContextActivityTest extends ActivityTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();

  private CreateContext controller;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return CreateContextActivity.class;
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(CreateContext.class);
  }

  @Test
  public void testCreate() throws Exception {
    FXPlatform.invokeLater(() -> controller.name.setText("hello"));
    FXPlatform.invokeLater(() -> controller.save());

    activityController.waitForTasks();
    List<Context> contexts = persistentWork.from(Context.class);
    assertEquals(1, contexts.size());
    assertEquals("hello", contexts.get(0).getName());
  }

  @Test
  public void testEdit() throws Exception {
    Context context = new Context("test");
    persistentWork.persist(context);
    CreateContextDS datasource = (CreateContextDS) store.getDatasource();
    datasource.setLoadingHint(context);
    activityController.reload();
    activityController.waitForTasks();

    assertEquals("test", controller.name.getText());

    FXPlatform.invokeLater(() -> controller.name.setText("hello"));
    FXPlatform.invokeLater(() -> controller.save());

    activityController.waitForTasks();
    List<Context> contexts = persistentWork.from(Context.class);
    assertEquals(1, contexts.size());
    assertEquals("hello", contexts.get(0).getName());
  }
}