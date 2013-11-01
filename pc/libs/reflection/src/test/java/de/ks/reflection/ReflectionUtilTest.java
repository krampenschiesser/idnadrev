package de.ks.reflection;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 */
public class ReflectionUtilTest {

  @Test
  public void testGetAllMethods() throws Exception {
    PropertyPath<Parent> parentPath = PropertyPath.of(Parent.class);
    PropertyPath<Child> childPath = PropertyPath.of(Child.class);

    List<Method> methods = ReflectionUtil.getAllMethods(Child.class);
    assertEquals(7, methods.size());


    childPath.build().childMethod();
    assertEquals(childPath.getLastMethod(), methods.get(0));

    assertEquals(ReflectionUtil.getMethod(Child.class, "overriden"), methods.get(1));

    assertEquals(ReflectionUtil.getMethod(Child.class, "privateChildMethod"), methods.get(2));

    childPath.build().protectedChildMethod();
    assertEquals(childPath.getLastMethod(), methods.get(3));

    parentPath.build().parentMethod();
    assertEquals(parentPath.getLastMethod(), methods.get(4));

    assertEquals(ReflectionUtil.getMethod(Parent.class, "privateParentMethod"), methods.get(5));

    parentPath.build().protectedParentMethod();
    assertEquals(parentPath.getLastMethod(), methods.get(6));
  }

  @Test
  public void testgetAllFields() throws Exception {
    List<Field> fields = ReflectionUtil.getAllFields(Child.class);
    assertEquals(6, fields.size());

    assertEquals("privateChildField", fields.get(0).getName());
    assertEquals("protectedChildField", fields.get(1).getName());
    assertEquals("publicChildField", fields.get(2).getName());
    assertEquals("privateParentField", fields.get(3).getName());
    assertEquals("protectedParentField", fields.get(4).getName());
    assertEquals("publicParentField", fields.get(5).getName());
  }

  @Test
  public void testGetClassHierarchy() throws Exception {
    List<Class<?>> hierarchy = ReflectionUtil.getClassHierarchy(Child.class);
    assertEquals(2, hierarchy.size());
    assertEquals(Child.class, hierarchy.get(0));
    assertEquals(Parent.class, hierarchy.get(1));
  }

  @Test
  public void testGetMethod() throws Exception {
    Method method = ReflectionUtil.getMethod(Parent.class, "privateParentMethod");
    assertNotNull(method);
    assertTrue(method.isAccessible());

    method = ReflectionUtil.getMethod(Child.class, "protectedParentMethod");
    assertNotNull(method);
    assertTrue(method.isAccessible());

    method = ReflectionUtil.getMethod(Child.class, "privateParentMethod");
    assertNotNull(method);
    assertTrue(method.isAccessible());
  }

  @Test
  public void testGetAllMethodsWithPredicates() throws Exception {
    List<Method> privateMethods = ReflectionUtil.getAllMethods(Child.class,//
            (Method m) -> m.getName().startsWith("private"),//
            (Method m) -> m.getDeclaringClass().equals(Parent.class)//
    );
    assertEquals(1, privateMethods.size());
    assertEquals("privateParentMethod", privateMethods.get(0).getName());
  }

  @Test
  public void testGetOverridenMethod() throws Exception {
    Method parent = ReflectionUtil.getMethod(Parent.class, "overriden");
    Method child = ReflectionUtil.getMethod(Child.class, "overriden");


    assertTrue(ReflectionUtil.getAllMethods(Parent.class).contains(parent));
    assertFalse(ReflectionUtil.getAllMethods(Child.class).contains(parent));
    assertTrue(ReflectionUtil.getAllMethods(Child.class).contains(child));
  }

  @Test
  public void testStaticField() throws Exception {
    List<Field> allFields = ReflectionUtil.getAllFields(Other.class);
    assertEquals(1, allFields.size());
    allFields = ReflectionUtil.getAllFields(Other.class, (Field f) -> !Modifier.isStatic(f.getModifiers()));
    assertEquals(0, allFields.size());
  }
}
