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

package de.ks.activity.callback;

import de.ks.activity.ActivityCfg;
import de.ks.activity.ModelBound;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.initialization.LoaderCallback;
import de.ks.javafx.NodeLookup;
import de.ks.reflection.ReflectionUtil;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public class InitializeModelBindings extends LoaderCallback {
  private static final Logger log = LoggerFactory.getLogger(InitializeModelBindings.class);
  private final ActivityCfg activityCfg;

  public InitializeModelBindings(ActivityCfg activityCfg) {
    this.activityCfg = activityCfg;
  }

  @Override
  public void accept(Object controller, Node node) {
    if (controller == null) {
      log.warn("Invoking model bindings for null controller. Node={}", node);
      return;
    }
    if (!controller.getClass().isAnnotationPresent(ModelBound.class)) {
      log.trace("Ignoring controller {} it has no @{} annotation.", controller.getClass(), ModelBound.class.getSimpleName());
      return;
    }
    ModelBound modelBound = controller.getClass().getAnnotation(ModelBound.class);
    String property = modelBound.property();
    Class<?> modelClass = modelBound.value();


    List<Field> allFields = ReflectionUtil.getAllFields(modelClass, (f) -> !Modifier.isStatic(f.getModifiers()));
    for (Field field : allFields) {
      String name = field.getName();
      Node found = NodeLookup.getChildNodeWithId(node, name);
      if (found != null) {
        log.debug("Found node {} for property '{}' for model class '{}' in {}", found, name, modelClass.getSimpleName(), node);

        ActivityStore activityStore = CDI.current().select(ActivityStore.class).get();
        activityStore.getBinding().addBoundProperty(name, modelClass, found);
      } else {
        log.debug("Did not find {} in {}", field.getName(), node);
        Set<Node> allPossibleNodes = NodeLookup.getAllIdNodes(node);
        allPossibleNodes.forEach((n) -> log.trace("\t\tGot available node {} with id {}", n.getClass().getSimpleName(), n.getId()));
      }
    }
  }

  @Override
  public void doInFXThread(Object controller, Node node) {

  }
}
