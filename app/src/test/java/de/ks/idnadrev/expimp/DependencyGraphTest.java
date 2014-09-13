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

package de.ks.idnadrev.expimp;

import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.idnadrev.entity.information.Information;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.metamodel.EntityType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class DependencyGraphTest {
  @Inject
  DependencyGraph graph;

  @Test
  public void testRootObjects() throws Exception {
    List<Collection<EntityType<?>>> importTasks = graph.getImportOrder();

    assertEquals(2, importTasks.size());
    assertTrue("Workunit not in 2nd stage", importTasks.get(1).stream().filter(t -> t.getJavaType().equals(WorkUnit.class)).findFirst().isPresent());
    assertTrue("Task not in 1nd stage", importTasks.get(0).stream().filter(t -> t.getJavaType().equals(Task.class)).findFirst().isPresent());
  }

  @Test
  public void testOptionalRelations() throws Exception {
    List<ToOneRelation> relations = graph.getOptionalToOneRelations();
    Optional<ToOneRelation> first = relations.stream().filter(r -> r.getDeclaringClass().equals(Information.class)).findFirst();
    assertFalse(first.isPresent());
  }
}