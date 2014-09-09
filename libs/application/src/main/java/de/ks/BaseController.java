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
package de.ks;

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.initialization.ActivityCallback;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.initialization.DatasourceCallback;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.validation.ValidationRegistry;
import javafx.fxml.Initializable;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseController<T> implements Initializable, DatasourceCallback<T>, ActivityCallback {
  @Inject
  protected ActivityController controller;
  @Inject
  protected ActivityStore store;
  @Inject
  protected ValidationRegistry validationRegistry;
  @Inject
  protected ActivityInitialization activityInitialization;

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  private void afterRefresh(ActivityLoadFinishedEvent e) {
    onRefresh(extractFromEvent(e));
  }

  protected T extractFromEvent(ActivityLoadFinishedEvent e) {
    return e.getModel();
  }

  @Override
  public abstract void initialize(URL location, ResourceBundle resources);

  @Override
  public void duringLoad(T model) {
    //
  }

  @Override
  public void duringSave(T model) {
    //
  }

  protected void onRefresh(T model) {
    //
  }
}
