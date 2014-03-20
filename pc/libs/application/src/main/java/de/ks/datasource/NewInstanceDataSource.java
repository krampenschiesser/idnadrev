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

package de.ks.datasource;

import de.ks.reflection.ReflectionUtil;

import java.util.function.Consumer;

/**
 *
 */
public class NewInstanceDataSource<M> implements DataSource<M> {
  protected final Class<M> modelClass;
  protected final Consumer<M> writeBack;

  public NewInstanceDataSource(Class<M> modelClass, Consumer<M> writeBack) {
    this.modelClass = modelClass;
    this.writeBack = writeBack;
  }

  @Override
  public M loadModel() {
    return ReflectionUtil.newInstance(modelClass);
  }

  @Override
  public void saveModel(M model) {
    writeBack.accept(model);
  }
}
