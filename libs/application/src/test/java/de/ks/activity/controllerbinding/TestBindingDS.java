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
package de.ks.activity.controllerbinding;

import de.ks.datasource.DataSource;
import de.ks.option.Option;
import de.ks.persistence.PersistentWork;

public class TestBindingDS implements DataSource<Option> {

  @Override
  public Option loadModel() {
    Option test = PersistentWork.forName(Option.class, "test");
    return test;
  }

  @Override
  public void saveModel(Option model) {
    PersistentWork.merge(model);
  }
}
