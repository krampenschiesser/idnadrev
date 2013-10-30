package de.ks.reflection;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
public class ReflectionUtil {
  private static final Logger log = LogManager.getLogger(ReflectionUtil.class);

  /**
   * Returns a List of all methods of this class and its supertypes without the methods of Object.
   * The list is ordered by hierarchy level (clazz first, then clazz.getSuperClass etc.)
   * In a hierarchy level the methods are ordered by their name.
   *
   * @param clazz
   * @return
   */
  public static List<Method> getAllMethods(Class<?> clazz) {
    ArrayList<Method> methods = new ArrayList<>(100);

    List<Class<?>> hierarchy = getClassHierarchy(clazz);
    for (Class<?> current : hierarchy) {
      List<Method> declaredMethods = Arrays.asList(current.getDeclaredMethods());
      Collections.sort(declaredMethods, getMethodComparator());
      methods.addAll(declaredMethods);
    }

    return methods;
  }

  public static List<Field> getAllFields(Class<?> clazz) {
    ArrayList<Field> fields = new ArrayList<>(100);

    List<Class<?>> hierarchy = getClassHierarchy(clazz);
    for (Class<?> current : hierarchy) {
      List<Field> declaredFields = Arrays.asList(current.getDeclaredFields());
      Collections.sort(declaredFields, getFieldComparator());
      fields.addAll(declaredFields);
    }

    return fields;
  }


  public static List<Class<?>> getClassHierarchy(Class<?> clazz) {
    ArrayList<Class<?>> classes = new ArrayList<>(25);
    for (Class<?> current = clazz; !Object.class.equals(current); current = current.getSuperclass()) {
      classes.add(current);
    }
    return classes;
  }

  public static Method getMethod(Class<?> clazz, String methodName) {
    try {
      Method method = clazz.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException e) {
      List<Method> allMethods = getAllMethods(clazz);
      Optional<Method> method = allMethods.parallelStream().filter((Method curr) -> curr.getName().equals(methodName)).findFirst();
      if (method.isPresent()) {
        method.get().setAccessible(true);
        return method.get();
      } else {
        log.error("Could not find method {}  in {}", methodName, clazz.getName());
        throw new RuntimeException(e);
      }
    }
  }

  protected static Comparator<Method> getMethodComparator() {
    return new Comparator<Method>() {
      @Override
      public int compare(Method o1, Method o2) {
        return o1.getName().compareTo(o2.getName());
      }
    };
  }

  protected static Comparator<Field> getFieldComparator() {
    return new Comparator<Field>() {
      @Override
      public int compare(Field o1, Field o2) {
        return o1.getName().compareTo(o2.getName());
      }
    };
  }
}
