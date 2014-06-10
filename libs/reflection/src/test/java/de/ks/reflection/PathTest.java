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

package de.ks.reflection;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PathTest {
  private PropertyPath path;

  @Before
  public void setup() {
    path = PropertyPath.of(PathObject.class);
  }

  @Test
  public void testSetter() {
    path.<PathObject>build().setContext(null);
    assertTrue(path.isSetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testGetter() {
    path.<PathObject>build().getContext();
    assertTrue(path.isGetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testSubSetter() {
    path.<PathObject>build().getContext().setName(null);
    assertTrue(path.isSetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testSubGetter() {
    path.<PathObject>build().getContext().getName();
    assertTrue(path.isGetter());
    assertTrue(path.isFieldAvailable());
  }

  @Test
  public void testGetGetterValue() {
    path.<PathObject>build().getContext();
    PathObject task = new PathObject("test");

    PathContext context = new PathContext();
    task.setContext(context.setName("hello"));

    PathContext value = path.getValue(task);
    assertEquals(context, value);
  }

  @Test
  public void testGetSubGetterValue() {
    path.<PathObject>build().getContext().getName();
    PathObject task = new PathObject("test");

    task.setContext(new PathContext().setName("hello"));

    String value = path.getValue(task);
    assertEquals("hello", value);
  }

  @Test
  public void testSetValue() {
    path.<PathObject>build().setName("dummy");
    PathObject task = new PathObject("test");
    path.setValue(task, "sauerland");

    assertEquals("sauerland", task.getName());
  }

  @Test
  public void testSetSubValue() {
    path.<PathObject>build().getContext().setName("dummy");

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

    path.<PathObject>build().getContext().setName("dummy");
    path.walk(task);
  }

  @Test
  public void testMethodName() throws Exception {
    assertEquals("getName", PropertyPath.methodName(File.class, (f) -> f.getName()));
  }

  @Test
  public void testPropertyName() throws Exception {
    assertEquals("name", PropertyPath.property(File.class, (f) -> f.getName()));
  }

  @Test
  public void testSetValueFromGetter() throws Exception {
    PropertyPath propertyPath = PropertyPath.of(PathObject.class, (p) -> p.getName());
    PathObject pathObject = new PathObject("test");
    propertyPath.setValue(pathObject, "Sauerland");
    assertEquals("Sauerland", pathObject.getName());
  }
}