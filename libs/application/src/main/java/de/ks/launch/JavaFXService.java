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
package de.ks.launch;

import de.ks.application.App;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;

public class JavaFXService extends Service {
  private static final Logger log = LoggerFactory.getLogger(JavaFXService.class);
  private String[] args;
  private Stage stage;
  private final CyclicBarrier barrier = new CyclicBarrier(2);

  @Override
  public void initialize(ExecutorService executorService, String[] args) {
    super.initialize(executorService, args);
    this.args = args;
  }

  @Override
  protected void doStart() {
    executorService.submit(() -> Application.launch(App.class, args));
    waitForJavaFXThread();
  }

  private void waitForJavaFXThread() {
    try {
      barrier.await();
    } catch (InterruptedException e) {
      log.error("Got interrupted ", e);
      throw new RuntimeException(e);
    } catch (BrokenBarrierException e) {
      log.error("Barrier broken ", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doStop() {
    Platform.exit();
    barrier.reset();
  }

  public Stage getStage() {
    return stage;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    stage.getScene().getWindow().hide();
    waitForJavaFXThread();
  }

  @Override
  public int getPriority() {
    return 2;
  }
}
