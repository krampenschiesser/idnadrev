package de.ks.reflection;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

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