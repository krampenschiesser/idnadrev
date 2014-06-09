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
package javafx.launch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class LaunchTest {
  private static final Logger log = LoggerFactory.getLogger(LaunchTest.class);

  private static final ExecutorService service = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
  private static final CountDownLatch latch = new CountDownLatch(1);

  public static final class TestApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
      log.info("Successfully launched!");
      primaryStage.setScene(new Scene(new StackPane()));
      latch.countDown();

    }
  }

  @Ignore
  @Test
  public void testLaunching() throws Exception {
    log.info("Start launching");
    service.submit(() -> Application.launch(TestApp.class));
    latch.await(2, TimeUnit.SECONDS);
    assertEquals(0, latch.getCount());
  }
}
