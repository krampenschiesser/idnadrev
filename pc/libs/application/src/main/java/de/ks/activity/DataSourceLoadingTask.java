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

package de.ks.activity;

import de.ks.activity.context.ActivityStore;
import de.ks.datasource.DataSource;
import de.ks.executor.ExecutorService;
import de.ks.executor.ThreadCallBoundValue;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.Set;

/**
 *
 */
public class DataSourceLoadingTask<M> extends Task<M> {
  private static final Logger log = LoggerFactory.getLogger(DataSourceLoadingTask.class);
  protected final DataSource<M> dataSource;
  private final Set<ThreadCallBoundValue> propagations;

  public DataSourceLoadingTask(DataSource<M> dataSource) {
    this.dataSource = dataSource;
    propagations = ExecutorService.instance.getPropagations().getPropagations();
    for (ThreadCallBoundValue propagation : propagations) {
      propagation.initializeInCallerThread();
    }
    setOnSucceeded((e) -> {
      for (ThreadCallBoundValue propagation : propagations) {
        propagation.doBeforeCallInTargetThread();
      }
      try {
        ActivityStore activityStore = CDI.current().select(ActivityStore.class).get();
        activityStore.setModel(getValue());
      } finally {
        for (ThreadCallBoundValue propagation : propagations) {
          propagation.doAfterCallInTargetThread();
        }
      }
    });
  }

  @Override
  protected M call() throws Exception {

    return dataSource.loadModel();

  }
}
