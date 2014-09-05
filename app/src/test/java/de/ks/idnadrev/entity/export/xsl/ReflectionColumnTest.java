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

package de.ks.idnadrev.entity.export.xsl;

import de.ks.idnadrev.entity.Thought;
import de.ks.reflection.ReflectionUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class ReflectionColumnTest {
  @Test
  public void testThoughtName() throws Exception {
    ReflectionColumn column = new ReflectionColumn(Thought.class, ReflectionUtil.getField(Thought.class, "name"));

    assertEquals(Cell.CELL_TYPE_STRING, column.getCellType());
    assertNotNull(column.getter);
    assertEquals("name", column.getIdentifier());

    assertEquals("bla", column.getValue(new Thought("bla")));
  }

  @Test
  public void testCreationTime() throws Exception {
    ReflectionColumn column = new ReflectionColumn(Thought.class, ReflectionUtil.getField(Thought.class, "creationTime"));
    Thought bla = new Thought("bla");
    Object value = column.getValue(bla);
    assertThat(value, instanceOf(LocalDateTime.class));
    assertEquals(value, bla.getCreationTime());
  }
}