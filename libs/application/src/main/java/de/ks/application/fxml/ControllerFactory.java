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

package de.ks.application.fxml;

import de.ks.activity.context.ActivityContext;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.reflection.ReflectionUtil;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 */
public class ControllerFactory implements Callback<Class<?>, Object> {
  private static final Logger log = LoggerFactory.getLogger(ControllerFactory.class);

  @Override
  public Object call(Class<?> clazz) {
    BeanManager beanManager = CDI.current().getBeanManager();
    for (Annotation annotation : clazz.getAnnotations()) {
      if (beanManager.isScope(annotation.annotationType())) {
        throw new IllegalStateException("Class " + clazz.getName() + " is not allowed to be in scope " + annotation + " because JavaFX can't inject fields in proxy types");
      }
    }

    Object object;
    Instance<?> instance = CDI.current().select(clazz);
    if (instance.isUnsatisfied()) {
      List<Field> injectedFields = ReflectionUtil.getAllFields(clazz, (f) -> f.isAnnotationPresent(Inject.class));
      if (!injectedFields.isEmpty()) {
        throw new IllegalArgumentException("Unable to instanitate class that defines injected fields but is no bean.");
      } else {
        Object newInstance = ReflectionUtil.newInstance(clazz, false);
        registerLoadedController(newInstance);
        return newInstance;
      }
    } else {
      object = instance.get();
    }

    registerLoadedController(object);
    return object;
  }

  protected void registerLoadedController(Object object) {
    CDI<Object> cdi = CDI.current();

    ActivityContext context = cdi.select(ActivityContext.class).get();
    if (context.hasCurrentActivity()) {
      Instance<ActivityInitialization> initializationInstance = cdi.select(ActivityInitialization.class);
      initializationInstance.get().addControllerToInitialize(object);
    }
  }
}
