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
package de.ks.activity.activitylink;

import de.ks.datasource.NewInstanceDataSource;

import java.util.function.Consumer;

public class ActivityBDS extends NewInstanceDataSource<ActivityBModel> {
  private ActivityBModel dataSourceHint;

  public ActivityBDS() {
    super(ActivityBModel.class);
  }

  @Override
  public ActivityBModel loadModel(Consumer<ActivityBModel> furtherProcessing) {
    ActivityBModel activityBModel = super.loadModel(furtherProcessing);
    if (dataSourceHint != null) {
      activityBModel.setDescription(dataSourceHint.getDescription());
    }
    furtherProcessing.accept(activityBModel);
    return activityBModel;
  }

  @Override
  public void saveModel(ActivityBModel model, Consumer<ActivityBModel> beforeSaving) {

  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof ActivityBModel) {
      this.dataSourceHint = (ActivityBModel) dataSourceHint;
    }
  }
}
