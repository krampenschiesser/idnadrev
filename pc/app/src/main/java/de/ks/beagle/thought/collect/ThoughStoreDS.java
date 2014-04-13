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
package de.ks.beagle.thought.collect;

import de.ks.beagle.entity.Thought;
import de.ks.datasource.NewInstanceDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThoughStoreDS extends NewInstanceDataSource<Thought> {
  private static final Logger log = LoggerFactory.getLogger(ThoughStoreDS.class);

  public ThoughStoreDS() {
    super(Thought.class);
  }

  @Override
  public void saveModel(Thought model) {
    log.info("Saving model {}", model);
  }
}
