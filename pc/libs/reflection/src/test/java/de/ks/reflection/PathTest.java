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

package de.ks.reflection;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PathTest {
  private PropertyPath<PathObject> path;

  @Before
  public void setup() {
    path = PropertyPath.of(PathObject.class);
  }

  @Test
  public void testSetter() {
    path.build().setContext(null);
    assertTrue(path.isSetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testGetter() {
    path.build().getContext();
    assertTrue(path.isGetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testSubSetter() {
    path.build().getContext().setName(null);
    assertTrue(path.isSetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testSubGetter() {
    path.build().getContext().getName();
    assertTrue(path.isGetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testGetGetterValue() {
    path.build().getContext();
    PathObject task = new PathObject("test");

    PathContext context = new PathContext();
    task.setContext(context.setName("hello"));

    PathContext value = path.getValue(task);
    assertEquals(context, value);
  }

  @Test
  public void testGetSubGetterValue() {
    path.build().getContext().getName();
    PathObject task = new PathObject("test");

    task.setContext(new PathContext().setName("hello"));

    String value = path.getValue(task);
    assertEquals("hello", value);
  }

  @Test
  public void testSetValue() {
    path.build().setName("dummy");
    PathObject task = new PathObject("test");
    path.setValue(task, "sauerland");

    assertEquals("sauerland", task.getName());
  }

  @Test
  public void testSetSubValue() {
    path.build().getContext().setName("dummy");

    PathObject task = new PathObject("test");
    task.setContext(new PathContext().setName("hello"));

    path.setValue(task, "sauerland");

    assertEquals("test", task.getName());
    assertEquals("sauerland", task.getContext().getName());
  }

  @Test
  public void testWalk() {
    PathObject task = new PathObject("test");
    task.setContext(new PathContext().setName("hello"));

    path.build().getContext().setName("dummy");
    path.walk(task);
  }
}