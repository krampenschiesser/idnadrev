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
package de.ks.executor;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Singleton
public class JavaFXExecutorService extends AbstractExecutorService {
  private static final Logger log = LoggerFactory.getLogger(JavaFXExecutorService.class);

  @Override
  public void shutdown() {
    //noop
  }

  @Override
  public List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

  @Override
  public boolean isShutdown() {
    return false;
  }

  @Override
  public boolean isTerminated() {
    return false;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return false;
  }

  @Override
  public void execute(Runnable command) {
    if (Platform.isFxApplicationThread()) {
      command.run();
    } else {
      Platform.runLater(command);
    }
  }

  public <T> T invokeInJavaFXThread(Callable<T> callable) {
    try {
      return submit(callable).get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Could not get value from {}", callable, e);
      throw new RuntimeException(e);
    }
  }
}
