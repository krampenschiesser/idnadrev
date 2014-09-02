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

package de.ks.persistence;

import de.ks.LauncherRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class SimplePersistenceTest {
  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(DummyEntity.class);
  }

  @Test
  public void testPersist() throws Exception {
    LocalDateTime begin = LocalDateTime.now();
    PersistentWork.persist(new DummyEntity("Hello World"));
    // read

    List<DummyEntity> result = PersistentWork.from(DummyEntity.class);
    DummyEntity readEntity = result.get(0);

    assertEquals("Hello World", readEntity.getName());
    assertNotNull(readEntity.getId());

    assertNotNull(readEntity.getCreationTime());
    assertEquals(begin.withNano(0), readEntity.getCreationTime().withNano(0));
    assertNull(readEntity.getUpdateTime());

    PersistentWork.wrap(() -> {
      DummyEntity reload = PersistentWork.reload(readEntity);
      reload.setMyDate(LocalDate.of(2014, 6, 3));
      reload.setMyTime(LocalTime.of(13, 42));
    });

    DummyEntity secondRead = PersistentWork.from(DummyEntity.class).get(0);
    assertNotNull(secondRead.getUpdateTime());
    assertEquals(LocalDate.of(2014, 6, 3), secondRead.getMyDate());
    assertEquals(LocalTime.of(13, 42), secondRead.getMyTime());
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
