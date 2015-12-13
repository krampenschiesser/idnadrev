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
package de.ks.blogging.grav;

import de.ks.blogging.grav.entity.GravBlog;
import de.ks.flatadocdb.session.Session;
import de.ks.flatjsondb.PersistentWork;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.standbein.activity.context.ActivityStore;
import de.ks.util.FXPlatform;
import org.junit.After;
import org.junit.Before;

import javax.inject.Inject;

public abstract class ActivityTest {
  @Inject
  protected ActivityController activityController;
  @Inject
  protected ActivityStore store;
  @Inject
  protected PersistentWork persistentWork;

  @Before
  public void startActivity() throws Exception {
    cleanup();
    persistentWork.run(session -> {
      try {
        createTestData(session);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    beforeActivityStart();
    activityController.startOrResume(new ActivityHint(getActivityClass()));
    activityController.waitForTasks();
    FXPlatform.waitForFX();
  }

  protected void beforeActivityStart() throws Exception {

  }

  protected abstract Class<? extends ActivityCfg> getActivityClass();

  @After
  public void shutdownActivity() throws Exception {
    activityController.stopAll();
  }

  protected void cleanup() {
    persistentWork.removeAllOf(GravBlog.class);
  }

  protected void createTestData(Session session) throws Exception {

  }
}
