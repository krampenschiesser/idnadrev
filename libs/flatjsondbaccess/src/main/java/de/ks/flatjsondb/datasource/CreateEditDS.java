/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.flatjsondb.datasource;

import de.ks.flatadocdb.entity.BaseEntity;
import de.ks.flatadocdb.session.Session;
import de.ks.flatjsondb.PersistentWork;
import de.ks.standbein.datasource.DataSource;
import de.ks.standbein.reflection.ReflectionUtil;

import javax.inject.Inject;
import java.util.function.Consumer;

public abstract class CreateEditDS<T extends BaseEntity> implements DataSource<T> {
  protected final Class<T> clazz;
  protected T hint;

  @Inject
  protected PersistentWork persistentWork;

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
      T read = persistentWork.read(em -> {
        T reloaded = persistentWork.reload(hint);
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
    persistentWork.run(session -> {
      T reloaded = persistentWork.reload(model);
      beforeSaving.accept(reloaded);
      if (reloaded.getId() == null) {
        session.persist(reloaded);
      }
      furtherSave(session, reloaded);
    });
    hint = null;
  }

  protected void furtherSave(Session session, T reloaded) {
  }
}
