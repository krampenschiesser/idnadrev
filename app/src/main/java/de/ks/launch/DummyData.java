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
package de.ks.launch;

import de.ks.beagle.entity.Context;
import de.ks.beagle.entity.Task;
import de.ks.beagle.entity.Thought;
import de.ks.beagle.entity.WorkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.ks.persistence.PersistentWork.persist;

public class DummyData extends Service {
  private static final Logger log = LoggerFactory.getLogger(DummyData.class);
  public static final String CREATE_DUMMYDATA = "create.dummydata";

  @Override
  public int getPriority() {
    return 2;
  }

  @Override
  protected void doStart() {
    if (Boolean.getBoolean(CREATE_DUMMYDATA)) {
      log.info("Creating dummy data.");
      persist(new Thought("Go fishing").setDescription("on a nice lake"));
      persist(new Thought("Go hiking").setDescription("maybe the CDT"));

      WorkType physical = new WorkType("physical");
      WorkType mental = new WorkType("mental");
      Context hiking = new Context("Hiking");

      Task backpack = new Task("Build a new backpack", "DIY").setProject(true);
      backpack.setContext(hiking);
      Task sketch = new Task("Create a sketch").setWorkType(mental);
      Task sew = new Task("Sew the backpack").setWorkType(physical);
      backpack.addChild(sketch);
      backpack.addChild(sew);

      persist(physical, mental, hiking, backpack, sketch, sew);
      persist(new Context("Work"), new Context("Studying"), new Context("Music"));
    }
  }

  @Override
  protected void doStop() {

  }
}
