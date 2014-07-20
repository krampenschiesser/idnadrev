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
package de.ks.idnadrev.thought.view;

import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.link.NavigationHint;
import de.ks.idnadrev.task.create.CreateTaskActivity;
import de.ks.menu.MenuItem;

import javax.enterprise.inject.spi.CDI;

@MenuItem(order = 0, value = "/main/thought")
public class ViewThoughtsActivity extends ActivityCfg {

  public ViewThoughtsActivity() {
    super(ViewThoughtsDS.class, ViewThoughts.class);

    NavigationHint navigationHint = new NavigationHint();
    navigationHint.setReturnToActivity(this);
    navigationHint.setDataSourceHint(() -> CDI.current().select(ActivityController.class).get().getControllerInstance(ViewThoughts.class).getSelectedThought());

    withActivity(ViewThoughts.class, "toTask", CreateTaskActivity.class, navigationHint);
  }
}
