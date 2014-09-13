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

import de.ks.LauncherRunner;
import de.ks.TempFileRule;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ViewThoughtsTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(ViewThoughtsTest.class);
  @Rule
  public TempFileRule testFiles = new TempFileRule(2);

  private ViewThoughts viewThoughts;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ViewThoughtsActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    PersistentWork.persist(new Thought("test"));
  }

  @Before
  public void setUp() throws Exception {
    viewThoughts = activityController.getCurrentController();
  }

  @Test
  public void testDeleteThought() throws Exception {
    assertEquals(1, viewThoughts.thoughtTable.getItems().size());
    FXPlatform.invokeLater(() -> viewThoughts.thoughtTable.getSelectionModel().select(0));

    viewThoughts.delete();

    List<Thought> from = PersistentWork.from(Thought.class);
    assertEquals(0, from.size());
  }
}
