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
package de.ks.executor.group;

import javafx.scene.control.TextInputControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class LastTextChange extends LastExecutionGroup<String> {
  private static final Logger log = LoggerFactory.getLogger(LastTextChange.class);
  public static final int WAIT_TIME = 100;
  private final TextInputControl textInput;
  private Consumer<CompletableFuture<String>> handler;

  public LastTextChange(TextInputControl textInput, ExecutorService executor) {
    this(textInput, WAIT_TIME, executor);
  }

  public LastTextChange(TextInputControl textInput, long waitTime, ExecutorService executor) {
    super("lastTextChange-" + textInput.getId(), waitTime, executor);
    this.textInput = textInput;

    textInput.textProperty().addListener((p, o, n) -> {
      if (n != null) {
        CompletableFuture<String> future = schedule(() -> n);
        if (future.getNumberOfDependents() == 0) {
          handler.accept(future);
        }
      }
    });
  }

  public void trigger() {
    CompletableFuture<String> future = schedule(() -> textInput.getText());
    if (future.getNumberOfDependents() == 0) {
      handler.accept(future);
    }
  }

  public void registerHandler(Consumer<CompletableFuture<String>> consumer) {
    handler = consumer;
  }
}
