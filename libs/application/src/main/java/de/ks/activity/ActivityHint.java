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
package de.ks.activity;

import de.ks.activity.context.ActivityStore;

import javax.enterprise.inject.spi.CDI;
import java.util.function.Function;
import java.util.function.Supplier;

public class ActivityHint {
  private Class<? extends ActivityCfg> nextActivity;
  private String nextActivityId;
  protected String returnToActivity;
  protected Supplier returnToDatasourceHint;
  protected Runnable returnToRunnable;
  protected Supplier dataSourceHint;

  protected boolean refreshOnReturn = true;

  public ActivityHint(Class<? extends ActivityCfg> activity) {
    this(activity, activity.getSimpleName(), null);
  }

  public ActivityHint(Class<? extends ActivityCfg> activity, String returnToActivity) {
    this(activity, activity.getSimpleName(), returnToActivity);
  }

  public ActivityHint(Class<? extends ActivityCfg> nextActivity, String nextActivityId, String returnToActivity) {
    this.nextActivity = nextActivity;
    this.nextActivityId = nextActivityId;
    this.returnToActivity = returnToActivity;
  }

  public String getReturnToActivity() {
    return returnToActivity;
  }

  public void setNextActivityId(String nextActivityId) {
    this.nextActivityId = nextActivityId;
  }

  public ActivityHint setReturnToActivity(String returnToActivity) {
    this.returnToActivity = returnToActivity;
    return this;
  }

  public ActivityHint returnToCurrent() {
    return setReturnToActivity(CDI.current().select(ActivityController.class).get().getCurrentActivityId());
  }

  public Supplier getReturnToDatasourceHint() {
    return returnToDatasourceHint;
  }

  public ActivityHint setReturnToDatasourceHint(Supplier returnToDatasourceHint) {
    this.returnToDatasourceHint = returnToDatasourceHint;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <M> ActivityHint setReturnToModelHint(Function<M, Object> modelFunction) {
    Object model = CDI.current().select(ActivityStore.class).get().getModel();
    this.returnToDatasourceHint = () -> modelFunction.apply((M) model);
    return this;
  }

  public Supplier getDataSourceHint() {
    return dataSourceHint;
  }

  public ActivityHint setDataSourceHint(Supplier dataSourceHint) {
    this.dataSourceHint = dataSourceHint;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <M> ActivityHint setModelHint(Function<M, Object> modelFunction) {
    Object model = CDI.current().select(ActivityStore.class).get().getModel();
    this.dataSourceHint = () -> modelFunction.apply((M) model);
    return this;
  }

  public Class<? extends ActivityCfg> getNextActivity() {
    return nextActivity;
  }

  public String getNextActivityId() {
    return nextActivityId;
  }

  @Override
  public String toString() {
    return "ActivityHint{" +
      "nextActivity=" + nextActivity +
      ", nextActivityId='" + nextActivityId + '\'' +
      ", returnToActivity=" + returnToActivity +
      ", returnToDatasourceHint=" + returnToDatasourceHint +
      ", dataSourceHint=" + dataSourceHint +
      ", refreshOnReturn=" + refreshOnReturn +
      '}';
  }

  public boolean isRefreshOnReturn() {
    return refreshOnReturn;
  }

  public ActivityHint setRefreshOnReturn(boolean refreshOnReturn) {
    this.refreshOnReturn = refreshOnReturn;
    return this;
  }

  public String getDescription() {
    return nextActivity.getSimpleName() + "-" + nextActivityId;
  }

  public Runnable getReturnToRunnable() {
    return returnToRunnable;
  }

  public ActivityHint setReturnToRunnable(Runnable returnToRunnable) {
    this.returnToRunnable = returnToRunnable;
    return this;
  }
}
