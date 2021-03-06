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

package de.ks.datasource;

import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.reflection.ReflectionUtil;

import javax.persistence.EntityManager;
import java.util.function.Consumer;

public class CreateEditDS<T extends AbstractPersistentObject<T>> implements DataSource<T> {
  protected final Class<T> clazz;
  protected T hint;

  public CreateEditDS(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint != null && clazz.isAssignableFrom(dataSourceHint.getClass())) {
      hint = ((T) dataSourceHint);
    } else {
      hint = null;
    }
  }

  @Override
  public T loadModel(Consumer<T> furtherProcessing) {
    if (hint != null) {
      T read = PersistentWork.read(em -> {
        T reloaded = PersistentWork.reload(hint);
        furtherProcessing.accept(reloaded);
        return reloaded;
      });
      return read;
    } else {
      T instance = getNewInstance();
      furtherProcessing.accept(instance);
      return instance;
    }
  }

  protected T getNewInstance() {
    return ReflectionUtil.newInstance(clazz);
  }

  @Override
  public void saveModel(T model, Consumer<T> beforeSaving) {
    PersistentWork.run(em -> {
      T reloaded = PersistentWork.reload(model);
      beforeSaving.accept(reloaded);
      furtherSave(em, reloaded);
      if (reloaded.getId() == 0) {
        em.persist(reloaded);
      }
    });
    hint = null;
  }

  protected void furtherSave(EntityManager em, T reloaded) {
  }
}
