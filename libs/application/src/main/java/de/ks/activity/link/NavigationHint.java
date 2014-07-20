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
package de.ks.activity.link;

import de.ks.activity.ActivityCfg;
import de.ks.activity.context.ActivityStore;

import javax.enterprise.inject.spi.CDI;
import java.util.function.Function;
import java.util.function.Supplier;

public class NavigationHint {
  protected ActivityCfg returnToActivity;
  protected Supplier returnToDatasourceHint;
  protected Supplier dataSourceHint;

  public NavigationHint() {
  }

  public NavigationHint(ActivityCfg returnToActivity, Supplier returnToDatasourceHint) {
    this.returnToDatasourceHint = returnToDatasourceHint;
    this.returnToActivity = returnToActivity;
  }

  public NavigationHint(ActivityCfg returnToActivity, Supplier returnToDatasourceHint, Supplier dataSourceHint) {
    this.dataSourceHint = dataSourceHint;
    this.returnToDatasourceHint = returnToDatasourceHint;
    this.returnToActivity = returnToActivity;
  }

  public NavigationHint(ActivityCfg returnToActivity) {
    this.returnToActivity = returnToActivity;
  }

  public ActivityCfg getReturnToActivity() {
    return returnToActivity;
  }

  public NavigationHint setReturnToActivity(ActivityCfg returnToActivity) {
    this.returnToActivity = returnToActivity;
    return this;
  }

  public Supplier getReturnToDatasourceHint() {
    return returnToDatasourceHint;
  }

  public NavigationHint setReturnToDatasourceHint(Supplier returnToDatasourceHint) {
    this.returnToDatasourceHint = returnToDatasourceHint;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <M> NavigationHint setReturnToModelHint(Function<M, Object> modelFunction) {
    Object model = CDI.current().select(ActivityStore.class).get().getModel();
    this.returnToDatasourceHint = () -> modelFunction.apply((M) model);
    return this;
  }

  public Supplier getDataSourceHint() {
    return dataSourceHint;
  }

  public NavigationHint setDataSourceHint(Supplier dataSourceHint) {
    this.dataSourceHint = dataSourceHint;
    return this;
  }

  @SuppressWarnings("unchecked")
  public <M> NavigationHint setModelHint(Function<M, Object> modelFunction) {
    Object model = CDI.current().select(ActivityStore.class).get().getModel();
    this.dataSourceHint = () -> modelFunction.apply((M) model);
    return this;
  }

  @Override
  public String toString() {
    return "NavigationHint{" +
            "returnToActivity=" + returnToActivity +
            ", returnToDatasourceHint=" + returnToDatasourceHint +
            ", dataSourceHint=" + dataSourceHint +
            '}';
  }
}
