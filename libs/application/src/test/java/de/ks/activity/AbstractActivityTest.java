/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.activity;

import de.ks.LauncherRunner;
import de.ks.activity.context.ActivityContext;
import de.ks.application.Navigator;
import de.ks.launch.JavaFXService;
import de.ks.launch.Launcher;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 *
 */
@RunWith(LauncherRunner.class)
public abstract class AbstractActivityTest {
  protected Navigator navigator;
  @Inject
  protected ActivityController activityController;
  protected ActivityCfg activityCfg;

  @Before
  public void setUp() throws Exception {
    JavaFXService service = Launcher.instance.getService(JavaFXService.class);
    navigator = Navigator.registerWithBorderPane(service.getStage());

    activityCfg = new ActivityCfg(DummyTestDataSource.class, ActivityHome.class);
    activityCfg.withLink(ActivityHome.class, "showDetails", Navigator.RIGHT_AREA, DetailController.class);
    activityCfg.withLink(ActivityHome.class, "switchView", OtherController.class);
    activityCfg.withLink(OtherController.class, "back", ActivityHome.class);
    activityCfg.withTask(DetailController.class, "pressMe", ActivityAction.class);
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(activityCfg);
    ActivityContext.stopAll();

  }

  public void save(ActivityModel model) {
    //
  }
}
