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
package de.ks.text.view;

import de.ks.activity.ActivityController;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.activity.initialization.LoadInFXThread;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.SuspendablePooledExecutorService;
import de.ks.text.AsciiDocParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@LoadInFXThread
public class AsciiDocViewer {
  public static CompletableFuture<DefaultLoader<Node, AsciiDocViewer>> load(Consumer<StackPane> viewConsumer, Consumer<AsciiDocViewer> controllerConsumer) {
    ActivityInitialization initialization = CDI.current().select(ActivityInitialization.class).get();
    return initialization.loadAdditionalController(AsciiDocViewer.class)//
            .thenApply(loader -> {
              viewConsumer.accept((StackPane) loader.getView());
              controllerConsumer.accept(loader.getController());
              return loader;
            });
  }

  private static final Logger log = LoggerFactory.getLogger(AsciiDocViewer.class);

  protected final Map<String, String> preloaded = new ConcurrentHashMap<>();
  protected final List<Function<String, String>> preProcessors = new ArrayList<>();
  @Inject
  ActivityController controller;
  @Inject
  AsciiDocParser parser;

  @FXML
  protected WebView webView;

  protected final SimpleStringProperty currentIdentifier = new SimpleStringProperty();

  public void reset() {
    webView.getEngine().loadContent("");
    currentIdentifier.set("");
  }

  public void show(AsciiDocContent content) {
    String identifier = content.getIdentifier();
    currentIdentifier.set(identifier);
    if (preloaded.containsKey(identifier)) {
      String html = preloaded.get(identifier);
      webView.getEngine().loadContent(html == null ? "" : html);
    } else {
      webView.getEngine().loadContent(content.getAdoc() == null ? "" : content.getAdoc());
    }
  }

  public void preload(Collection<AsciiDocContent> load) {
    SuspendablePooledExecutorService executorService = controller.getCurrentExecutorService();
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();
    load.forEach(t -> {
      CompletableFuture<Pair<String, String>> completableFuture = CompletableFuture.supplyAsync(() -> preProcess(t.getAdoc()), executorService)//
              .thenApply(desc -> parser.parse(desc))//
              .thenApply(html -> Pair.of(t.getIdentifier(), html));
      completableFuture.thenApply(pair -> {
        preloaded.put(pair.getKey(), pair.getValue());
        return pair;
      }).thenAcceptAsync(pair -> {
        if (currentIdentifier.getValueSafe().equals(pair.getKey())) {
          webView.getEngine().loadContent(pair.getValue());
        }
      }, javaFXExecutor);
    });
  }

  protected String preProcess(String input) {
    for (Function<String, String> preProcessor : preProcessors) {
      input = preProcessor.apply(input);
    }
    return input;
  }

  public void addPreProcessor(Function<String, String> processor) {
    this.preProcessors.add(processor);
  }
}
