/*
 * Copyright [2014] [Christian Loehnert]
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
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.Activity;
import de.ks.activity.ModelBound;
import de.ks.reflection.ReflectionUtil;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.BiConsumer;

/**
 *
 */
public class InitializeModelBindings implements BiConsumer<Object, Node> {
  private static final Logger log = LoggerFactory.getLogger(InitializeModelBindings.class);
  private final Activity activity;

  public InitializeModelBindings(Activity activity) {
    this.activity = activity;
  }

  @Override
  public void accept(Object controller, Node node) {
    if (!controller.getClass().isAnnotationPresent(ModelBound.class)) {
      return;
    }
    ModelBound modelBound = controller.getClass().getAnnotation(ModelBound.class);
    String property = modelBound.property();
    Class<?> modelClass = modelBound.value();

    List<Field> allFields = ReflectionUtil.getAllFields(modelClass, (f) -> !Modifier.isStatic(f.getModifiers()));
    for (Field field : allFields) {
      String name = field.getName();
      Node found = node.lookup("#" + name);
      if (found != null) {
        log.debug("Found node {} for property '{}' for model class '{}' in {}", found, name, modelClass.getSimpleName(), node);
      }
    }
    //TODO create bindings based on observable properties (maybe read from activity?)
    //TODO register properties on activity for later manipulation
  }
}
