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

import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.activity.context.ActivityStore;
import de.ks.blogging.grav.entity.GravBlog;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.After;
import org.junit.Before;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public abstract class ActivityTest {
  @Inject
  protected ActivityController activityController;
  @Inject
  protected ActivityStore store;

  @Before
  public void startActivity() throws Exception {
    cleanup();
    PersistentWork.run(em -> {
      try {
        createTestData(em);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    activityController.startOrResume(new ActivityHint(getActivityClass()));
    activityController.waitForTasks();
    FXPlatform.waitForFX();
  }

  protected abstract Class<? extends ActivityCfg> getActivityClass();

  @After
  public void shutdownActivity() throws Exception {
    activityController.stopAll();
  }

  protected void cleanup() {
    PersistentWork.deleteAllOf(GravBlog.class);
  }

  protected void createTestData(EntityManager em) throws Exception {

  }
}
