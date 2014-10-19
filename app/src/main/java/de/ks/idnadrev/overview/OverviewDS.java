/**
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
package de.ks.idnadrev.overview;

import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.Context;
import de.ks.persistence.PersistentWork;

import java.util.List;
import java.util.function.Consumer;

public class OverviewDS implements DataSource<OverviewModel> {
  @Override
  public OverviewModel loadModel(Consumer<OverviewModel> furtherProcessing) {
    return PersistentWork.read(em -> {
      OverviewModel model = new OverviewModel();
      List<Context> contexts = PersistentWork.from(Context.class);
      model.getContexts().addAll(contexts);
      return model;
    });
  }

  @Override
  public void saveModel(OverviewModel model, Consumer<OverviewModel> beforeSaving) {

  }
}
