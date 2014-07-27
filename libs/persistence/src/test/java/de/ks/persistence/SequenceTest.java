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
import de.ks.persistence.entity.Sequence;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class SequenceTest {
  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Sequence.class);
  }

  @Test
  public void testSequence() throws Exception {
    assertEquals(1, Sequence.getNextSequenceNr("test1"));
    assertEquals(2, Sequence.getNextSequenceNr("test1"));
    assertEquals(3, Sequence.getNextSequenceNr("test1"));
  }

  @Test
  public void testMultithreadedAccess() throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(4);
    Sequence.getNextSequenceNr("test1");

    LinkedList<Future<List<Long>>> futures = new LinkedList<>();

    for (int i = 0; i < 4; i++) {
      Future<List<Long>> future = pool.submit(() -> {
        List<Long> ids = new ArrayList<Long>(100);
        for (int j = 0; j < 100; j++) {
          ids.add(Sequence.getNextSequenceNr("test1"));
        }
        return ids;
      });
      futures.add(future);
    }

    Set<Long> uniqueIds = new HashSet<>();
    List<Long> allIds = new ArrayList<>(400);

    for (Future<List<Long>> future : futures) {
      uniqueIds.addAll(future.get());
      allIds.addAll(future.get());
    }

    assertEquals(allIds.size(), uniqueIds.size());
  }
}
