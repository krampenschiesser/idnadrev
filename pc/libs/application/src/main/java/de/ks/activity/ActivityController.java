/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.activity;


import de.ks.activity.context.ActivityContext;
import de.ks.activity.context.ActivityStore;
import de.ks.executor.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Deque;
import java.util.LinkedList;

/**
 * used to control different activities and their interaction
 */
@Singleton
public class ActivityController {
  @Inject
  protected ActivityContext context;
  @Inject
  protected ActivityStore store;
  @Inject
  protected ExecutorService executorService;

  protected final Deque<Activity> activities = new LinkedList<>();

  public void start(Activity activity) {
    context.startActivity(activity.toString());
    activity.start();
    executorService.submit(new DataSourceLoadingTask<>(activity.getDataSource()));
    activities.add(activity);
  }

  public void stopCurrentResumeLast() {
    context.stopActivity(getCurrentActivity().toString());
  }

  public Activity getCurrentActivity() {
    return activities.getLast();
  }
}
