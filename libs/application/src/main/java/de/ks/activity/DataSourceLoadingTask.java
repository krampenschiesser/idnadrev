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

import de.ks.activity.context.ActivityStore;
import de.ks.datasource.DataSource;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class DataSourceLoadingTask<M> extends Task<M> {
  private static final Logger log = LoggerFactory.getLogger(DataSourceLoadingTask.class);
  protected final DataSource<M> dataSource;
  protected final CountDownLatch latch = new CountDownLatch(2);

  public DataSourceLoadingTask(DataSource<M> dataSource) {
    this.dataSource = dataSource;

    setOnSucceeded((e) -> {
      try {
        M value = getValue();
        log.debug("Loaded model '{}'", value);
        CDI.current().select(ActivityStore.class).get().setModel(value);
      } finally {
        latch.countDown();
      }
    });
  }

  @Override
  protected M call() throws Exception {
    try {
      return dataSource.loadModel();
    } finally {
      latch.countDown();
    }
  }

  public void await() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
