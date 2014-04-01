/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.beagle.thought;


import de.ks.activity.Activity;
import de.ks.activity.ActivityController;
import de.ks.application.Navigator;
import de.ks.beagle.entity.Thought;
import de.ks.datasource.NewInstanceDataSource;
import de.ks.menu.MenuItem;

import javax.inject.Inject;

@MenuItem("/main/activity")
public class ThoughtActivity extends Activity {
  @Inject
  public ThoughtActivity(ActivityController activityController, Navigator navigator) {
    super(new NewInstanceDataSource<>(Thought.class, null), AddThought.class, activityController, navigator);

    configure();
  }

  private void configure() {
    withTask(getInitialController(), "save", SaveThought.class);
  }
}