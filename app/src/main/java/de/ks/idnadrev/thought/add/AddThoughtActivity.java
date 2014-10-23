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

package de.ks.idnadrev.thought.add;

import de.ks.activity.ActivityCfg;
import de.ks.menu.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MenuItem(order = 10, value = "/main/thought")
public class AddThoughtActivity extends ActivityCfg {
  private static final Logger log = LoggerFactory.getLogger(AddThoughtActivity.class);

  public AddThoughtActivity() {
    super(AddThoughtDS.class, AddThought.class);
  }
}
