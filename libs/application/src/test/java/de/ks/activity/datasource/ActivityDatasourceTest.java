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
package de.ks.activity.datasource;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.activity.context.ActivityStore;
import de.ks.application.Navigator;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(LauncherRunner.class)
public class ActivityDatasourceTest {
  private static final Logger log = LoggerFactory.getLogger(ActivityDatasourceTest.class);
  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;

  @Before
  public void setUp() throws Exception {
    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    Navigator.registerWithBorderPane(service.getStage());
  }

  @After
  public void tearDown() throws Exception {
    controller.stopAll();
  }

  @Test
  public void testDatasourceHint() throws Exception {
    ActivityHint activityHint = new ActivityHint(DatasourceActivity.class);

    controller.startOrResume(activityHint);
    TestDataSource initialDS = (TestDataSource) store.getDatasource();
    assertNull(initialDS.getDataSourceHint());

    ActivityHint nextActivity = new ActivityHint(DatasourceActivity.class);
    nextActivity.setNextActivityId("other");
    nextActivity.setDataSourceHint(() -> "Hallo sauerland!");
    nextActivity.setReturnToActivity(activityHint.getNextActivityId());
    nextActivity.setReturnToDatasourceHint(() -> "back");

    controller.startOrResume(nextActivity);
    TestDataSource datasource = (TestDataSource) store.getDatasource();
    assertEquals("Hallo sauerland!", datasource.getDataSourceHint());

    controller.stopCurrent();

    assertEquals("back", initialDS.getDataSourceHint());
  }

  @Test
  public void testResume() throws Exception {
    ActivityHint activityHint = new ActivityHint(DatasourceActivity.class);
    controller.startOrResume(activityHint);

    ActivityHint nextActivity = new ActivityHint(DatasourceActivity.class);
    nextActivity.setNextActivityId("other");
    controller.startOrResume(nextActivity);

    ActivityHint returnToInitial = new ActivityHint(DatasourceActivity.class);
    returnToInitial.setDataSourceHint(() -> "PCT");
    controller.startOrResume(returnToInitial);

    TestDataSource datasource = (TestDataSource) store.getDatasource();
    assertEquals("PCT", datasource.getDataSourceHint());
  }
}
