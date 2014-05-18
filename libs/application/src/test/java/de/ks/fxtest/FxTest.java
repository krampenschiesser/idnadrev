/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.fxtest;


import de.ks.LauncherRunner;
import de.ks.application.fxml.DefaultLoader;
import javafx.scene.layout.StackPane;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

/**
 *
 */
@RunWith(LauncherRunner.class)
public class FxTest {
  private static final Logger log = LoggerFactory.getLogger(FxTest.class);

  @Test
  public void testName() throws Exception {
    Profiler profiler = new Profiler("fxml loading of the same file");
    profiler.setLogger(log);
    int MAX = 100;
    for (int i = 0; i < MAX; i++) {
      DefaultLoader<StackPane, FxTestController> loader = new DefaultLoader<>(FxTestController.class);
      loader.getController();
    }
    profiler.stop().log();
    log.info("Average time: {}ms", profiler.elapsedTime() / 1000 / 1000 / MAX);
  }
}
