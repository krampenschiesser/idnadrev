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
package de.ks.activity.datasource;

import de.ks.datasource.DataSource;

import java.util.function.Consumer;

public class TestDataSource implements DataSource<Object> {
  private Object dataSourceHint;

  @Override
  public Object loadModel(Consumer<Object> furtherProcessing) {
    return null;
  }

  @Override
  public void saveModel(Object model, Consumer<Object> beforeSaving) {

  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    this.dataSourceHint = dataSourceHint;
  }

  public Object getDataSourceHint() {
    return dataSourceHint;
  }
}
