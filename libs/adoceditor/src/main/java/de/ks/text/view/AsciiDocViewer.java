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
import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.initialization.ActivityCallback;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.JavaFXExecutorService;
import de.ks.text.AsciiDocParser;
import de.ks.text.process.AsciiDocPreProcessor;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AsciiDocViewer implements Initializable, ActivityCallback {

  public static final String DEFAULT = "default";

  public static CompletableFuture<DefaultLoader<Node, AsciiDocViewer>> load(Consumer<StackPane> viewConsumer, Consumer<AsciiDocViewer> controllerConsumer) {
    ActivityInitialization initialization = CDI.current().select(ActivityInitialization.class).get();
    return initialization.loadAdditionalControllerWithFuture(AsciiDocViewer.class)//
      .thenApply(loader -> {
        viewConsumer.accept((StackPane) loader.getView());
        controllerConsumer.accept(loader.getController());
        return loader;
      });
  }

  private static final Logger log = LoggerFactory.getLogger(AsciiDocViewer.class);

  protected final Map<String, String> preloaded = new ConcurrentHashMap<>();
  protected final List<AsciiDocPreProcessor> preProcessors = new ArrayList<>();
  @Inject
  ActivityController controller;
  @Inject
  AsciiDocParser parser;
  @Inject
  Instance<AsciiDocPreProcessor> preProcessorProvider;

  @FXML
  protected StackPane root;
  protected WebView webView;

  protected final SimpleStringProperty currentHtml = new SimpleStringProperty();
  protected final SimpleStringProperty currentIdentifier = new SimpleStringProperty();

  protected Set<Runnable> supensionRunnables = new HashSet<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CompletableFuture.supplyAsync(() -> new WebView(), controller.getJavaFXExecutor()).thenAccept(view -> {
      webView = view;
      webView.setMinSize(100, 100);
      webView.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      root.getChildren().add(webView);
    });

    Iterator<AsciiDocPreProcessor> iterator = preProcessorProvider.iterator();
    while (iterator.hasNext()) {
      AsciiDocPreProcessor next = iterator.next();
      addPreProcessor(next);
    }
  }

  public void reset() {
    webView.getEngine().loadContent("");
    currentIdentifier.set("");
  }

  public void showDirect(String content) {
    currentIdentifier.set(DEFAULT);
    preloaded.remove(DEFAULT);
    preload(Collections.singletonList(new AsciiDocContent(DEFAULT, content)));
  }

  public void show(AsciiDocContent content) {
    String identifier = content.getIdentifier();
    currentIdentifier.set(identifier);
    if (preloaded.containsKey(identifier)) {
      String html = preloaded.get(identifier);
      currentHtml.set(html);
      webView.getEngine().loadContent(html == null ? "" : html);
    } else {
      preload(Collections.singletonList(content));
      webView.getEngine().loadContent(content.getAdoc() == null ? "" : content.getAdoc());
    }
  }

  public void preload(Collection<AsciiDocContent> load) {
    ActivityExecutor executorService = controller.getExecutorService();
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();
    load.forEach(t -> {
      CompletableFuture<Pair<String, String>> completableFuture = CompletableFuture.supplyAsync(() -> preProcess(t.getAdoc()), executorService)//
        .thenApply(desc -> {
          if (desc == null || desc.trim().isEmpty()) {
            return "";
          } else {
            return parser.parse(desc);
          }
        })//
        .thenApply(html -> Pair.of(t.getIdentifier(), html));
      completableFuture.thenApply(pair -> {
        preloaded.put(pair.getKey(), pair.getValue());
        return pair;
      }).thenAcceptAsync(pair -> {
        if (currentIdentifier.getValueSafe().equals(pair.getKey())) {
          String html = pair.getValue();
          currentHtml.set(html);
          webView.getEngine().loadContent(html);
        }
      }, javaFXExecutor)//
        .exceptionally(e -> {
          log.error("Could not parse adoc", e);
          return null;
        });
    });
  }

  protected String preProcess(String input) {
    for (AsciiDocPreProcessor preProcessor : preProcessors) {
      input = preProcessor.preProcess(input, this);
    }
    return input;
  }

  public void addPreProcessor(AsciiDocPreProcessor processor) {
    this.preProcessors.add(processor);
  }

  public void clear() {
    preloaded.clear();
  }

  public void requestFocus() {
    webView.requestFocus();
  }

  public WebView getWebView() {
    return webView;
  }

  public String getCurrentHtml() {
    return currentHtml.get();
  }

  public SimpleStringProperty currentHtmlProperty() {
    return currentHtml;
  }

  public void setCurrentHtml(String currentHtml) {
    this.currentHtml.set(currentHtml);
  }

  public void addSuspensionRunnable(Runnable r) {
    supensionRunnables.add(r);
  }

  @Override
  public void onSuspend() {
    supensionRunnables.forEach(r -> r.run());
  }

  @Override
  public void onStop() {
    onSuspend();
  }
}
