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

import de.ks.util.Predicates;
import javafx.util.Pair;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
public class ReflectionUtil {
  private static final Logger log = LoggerFactory.getLogger(ReflectionUtil.class);

  /**
   * Returns a List of all methods of this class and its supertypes without the methods of Object.
   * The list is ordered by hierarchy level (clazz first, then clazz.getSuperClass etc.)
   * In a hierarchy level the methods are ordered by their name.
   *
   * @param clazz
   * @param predicates combined with AND
   * @return
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static List<Method> getAllMethods(Class<?> clazz, Predicate<Method>... predicates) {
    ArrayList<Method> methods = new ArrayList<>(100);
    Map<Pair<String, List<Class<?>>>, Method> collected = new HashMap<>();

    List<Class<?>> hierarchy = getClassHierarchy(clazz, true);
    for (Class<?> current : hierarchy) {
      List<Method> declaredMethods = Arrays.asList(current.getDeclaredMethods());
      Collections.sort(declaredMethods, getMethodComparator());

      for (Method declaredMethod : declaredMethods) {
        String methodName = declaredMethod.getName();
        Pair<String, List<Class<?>>> key = new Pair<>(methodName, Arrays.asList(declaredMethod.getParameterTypes()));

        if (!collected.containsKey(key)) {
          collected.put(new Pair<>(methodName, Arrays.asList(declaredMethod.getParameterTypes())), declaredMethod);
          methods.add(declaredMethod);
        }
      }
    }

    Predicate<Method> combinedPredicate = Predicates.combineAnd(predicates);
    if (combinedPredicate != null) {
      return methods.stream().distinct().filter(combinedPredicate).collect(Collectors.toList());
    } else {
      return methods.stream().distinct().collect(Collectors.toList());
    }
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static List<Field> getAllFields(Class<?> clazz, Predicate<Field>... predicates) {
    ArrayList<Field> fields = new ArrayList<>(100);

    List<Class<?>> hierarchy = getClassHierarchy(clazz, false);
    Collections.reverse(hierarchy);
    for (Class<?> current : hierarchy) {
      List<Field> declaredFields = Arrays.asList(current.getDeclaredFields());
      Collections.sort(declaredFields, getFieldComparator());
      fields.addAll(declaredFields);
    }

    Predicate<Field> combinedPredicate = Predicates.combineAnd(predicates);
    if (combinedPredicate != null) {
      return fields.stream().filter(combinedPredicate).collect(Collectors.toList());
    } else {
      return fields;
    }
  }

  public static List<Class<?>> getClassHierarchy(Class<?> clazz, boolean includeInterfaces) {
    Predicate<Class<?>> filter = new Predicate<Class<?>>() {
      @Override
      public boolean test(Class<?> clazz) {
        boolean retval = !Object.class.equals(clazz);
        retval = retval && (includeInterfaces ? true : !clazz.isInterface());
        return retval;
      }
    };
    ArrayList<Class<?>> classes = new ArrayList<>(25);
    try {

      for (Class<?> current = clazz; filter.test(current); current = current.getSuperclass()) {
        classes.add(current);
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
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
    return (o1, o2) -> o1.getName().compareTo(o2.getName());
  }

  protected static Comparator<Field> getFieldComparator() {
    return (o1, o2) -> o1.getName().compareTo(o2.getName());
  }

  public static Object invokeMethod(Method method, Object target, Object... parameters) {
    try {
      method.setAccessible(true);
      return method.invoke(target, parameters);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.error("Could not invoke method {} of {}", method.getName(), target.getClass().getName(), e);
      throw new RuntimeException(e);
    }
  }

  public static void setField(Field field, Object instance, Object value) {
    try {
      field.setAccessible(true);
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      log.error("Could not set field {}", field, e);
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> clazz) {
    return newInstance(clazz, true);
  }

  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> clazz, boolean useObjenesis) {
    Exception caught = null;
    try {
      Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      for (Constructor<?> constructor : constructors) {
        if (constructor.getParameterTypes().length == 0) {
          constructor.setAccessible(true);
          return (T) constructor.newInstance();
        }
      }
    } catch (Exception e) {
      log.trace("Could not create a new instance of {} by using the default constructor.", clazz.getName(), e);
      caught = e;
    }
    if (useObjenesis) {
      return new ObjenesisStd().newInstance(clazz);
    } else {
      throw new RuntimeException(caught);
    }
  }

  public static Object getFieldValue(Object object, String fieldName) {
    Field field = getField(object, fieldName);
    return getFieldValue(object, field);
  }

  public static Object getFieldValue(Object object, Field field) {
    if (field == null) {
      return null;
    }
    field.setAccessible(true);
    try {
      return field.get(object);
    } catch (IllegalAccessException e) {
      log.debug("Could nto get field {} from {}", field, object.getClass().getSimpleName(), e);
      return null;
    }
  }

  public static Field getField(Object object, String fieldName) {
    Class<?> clazz = object.getClass();
    return getField(clazz, fieldName);
  }

  public static Field getField(Class<?> clazz, String fieldName) {
    List<Field> allFields = getAllFields(clazz, f -> f.getName().equals(fieldName));
    Field field = null;
    if (allFields.size() == 1) {
      field = allFields.get(0);
    }
    return field;
  }
}
