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

package de.ks.persistence;


import de.ks.CDIRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@RunWith(CDIRunner.class)
public class SimplePersistenceTest {
  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(DummyEntity.class);
  }

  @Test
  public void testPersist() throws Exception {
    PersistentWork.persist(new DummyEntity("Hello World"));
    // read

    List<DummyEntity> result = PersistentWork.from(DummyEntity.class);
    DummyEntity readEntity = result.get(0);

    assertEquals("Hello World", readEntity.getName());
  }

  @Test
  public void testTransactionFailed() {
    try {
      PersistentWork.run(em -> {
        DummyEntity entity = new DummyEntity("Hello World");
        em.persist(entity);
        throw new RuntimeException();
      });
    } catch (RuntimeException e) {
      //ok!
    }

    assertTrue(PersistentWork.from(DummyEntity.class).isEmpty());
  }
}
