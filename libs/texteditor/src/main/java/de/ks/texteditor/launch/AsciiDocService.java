/*
 * Copyright [2016] [Christian Loehnert]
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

package de.ks.texteditor.launch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.standbein.launch.Service;
import org.asciidoctor.Asciidoctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.*;

@Singleton
public class AsciiDocService extends Service {
  private static final Logger log = LoggerFactory.getLogger(AsciiDocService.class);
  protected final ExecutorService executorService = new ThreadPoolExecutor(0, 1, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).build());
  private Future<?> asciiDocParserFuture;

  @Inject
  MetaData metaData;

  @Override
  protected void doStart() {
    metaData.extract();
    asciiDocParserFuture = executorService.submit(() -> Asciidoctor.Factory.create());
  }

  public void waitInitialized() {
    try {
      if (!asciiDocParserFuture.isDone()) {
        log.info("Waiting for asciidoctor to load....");
      }
      asciiDocParserFuture.get();
      log.info("Ascidoctor loaded successfully.");
    } catch (InterruptedException | ExecutionException e) {
      log.error("Could not load asciiDocParser", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doStop() {
    executorService.shutdownNow();
    try {
      executorService.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Could not stop executor service");
    }
  }
}
