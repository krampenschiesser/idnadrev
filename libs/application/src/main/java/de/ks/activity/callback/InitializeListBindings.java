/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.activity.callback;

import de.ks.activity.ActivityCfg;
import de.ks.activity.ListBound;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.initialization.LoaderCallback;
import de.ks.reflection.ReflectionUtil;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class InitializeListBindings extends LoaderCallback {
  private static final Logger log = LoggerFactory.getLogger(InitializeListBindings.class);
  private final ActivityCfg activityCfg;

  public InitializeListBindings(ActivityCfg activityCfg) {
    this.activityCfg = activityCfg;
  }

  @Override
  public void accept(Object controller, Node node) {
    if (controller == null) {
      log.warn("Invoking list bindings for null controller. Node={}", node);
      return;
    }
    if (!controller.getClass().isAnnotationPresent(ListBound.class)) {
      return;
    }
    ListBound modelBound = controller.getClass().getAnnotation(ListBound.class);
    String property = modelBound.property();
    Class<?> modelClass = modelBound.value();

    ActivityStore store = CDI.current().select(ActivityStore.class).get();

    if (property.equals("this")) {
      Node table = node.lookup("#_this");
      log.debug("Found node {} for property '{}' for model class '{}' in {}", table, property, modelClass.getSimpleName(), node);
      store.getBinding().addBoundProperty(property, List.class, table);
      return;
    }

    List<Field> allFields = ReflectionUtil.getAllFields(modelClass, (f) -> !Modifier.isStatic(f.getModifiers()));
    for (Field field : allFields) {
      String name = field.getName();
      Node found = node.lookup("#" + name);
      if (found != null) {
        log.debug("Found node {} for property '{}' for model class '{}' in {}", found, name, modelClass.getSimpleName(), node);

        store.getBinding().addBoundProperty(name, modelClass, found);
      }
    }
  }

  @Override
  public void doInFXThread(Object controller, Node node) {

  }
}
