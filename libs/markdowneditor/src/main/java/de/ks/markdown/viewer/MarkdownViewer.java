/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.markdown.viewer;

import de.ks.activity.ActivityController;
import de.ks.activity.executor.ActivityExecutor;
import de.ks.activity.initialization.ActivityCallback;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.application.fxml.DefaultLoader;
import de.ks.executor.JavaFXExecutorService;
import de.ks.markdown.MarkdownParser;
import javafx.beans.property.SimpleObjectProperty;
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

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MarkdownViewer implements Initializable, ActivityCallback {
  public static CompletableFuture<DefaultLoader<Node, MarkdownViewer>> load(Consumer<StackPane> viewConsumer, Consumer<MarkdownViewer> controllerConsumer) {
    ActivityInitialization initialization = CDI.current().select(ActivityInitialization.class).get();
    return initialization.loadAdditionalControllerWithFuture(MarkdownViewer.class)//
      .thenApply(loader -> {
        viewConsumer.accept((StackPane) loader.getView());
        controllerConsumer.accept(loader.getController());
        return loader;
      });
  }

  private static final Logger log = LoggerFactory.getLogger(MarkdownViewer.class);

  public static final String DEFAULT = "default";

  protected final Map<String, String> preloaded = new ConcurrentHashMap<>();

  protected final SimpleStringProperty currentHtml = new SimpleStringProperty();
  protected final SimpleStringProperty currentIdentifier = new SimpleStringProperty();

  @Inject
  ActivityController controller;
  @Inject
  MarkdownParser parser;

  @FXML
  protected StackPane root;
  protected WebView webView;

  protected final SimpleObjectProperty<File> file = new SimpleObjectProperty();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    CompletableFuture.supplyAsync(() -> new WebView(), controller.getJavaFXExecutor()).thenAccept(view -> {
      webView = view;
      webView.setMinSize(100, 100);
      webView.setPrefSize(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE);
      root.getChildren().add(webView);
    });

  }

  public void clear() {
    preloaded.clear();
  }

  public void reset() {
    webView.getEngine().loadContent("");
    currentIdentifier.set("");
  }

  public void requestFocus() {
    webView.requestFocus();
  }

  public void showDirect(String content) {
    currentIdentifier.set(DEFAULT);
    preloaded.remove(DEFAULT);
    preload(Collections.singletonList(new MarkdownContent(DEFAULT, content)));
  }

  public void showDirect(File content) {
    currentIdentifier.set(DEFAULT);
    preloaded.remove(DEFAULT);
    preload(Collections.singletonList(new MarkdownContent(DEFAULT, content)));
  }

  public void show(MarkdownContent content) {
    String identifier = content.getIdentifier();
    currentIdentifier.set(identifier);
    if (preloaded.containsKey(identifier)) {
      String html = preloaded.get(identifier);
      currentHtml.set(html);
      webView.getEngine().loadContent(html == null ? "" : html);
    } else {
      preload(Collections.singletonList(content));
      webView.getEngine().loadContent(content.getMarkdown() == null ? "" : content.getMarkdown());
    }
  }

  public void preload(Collection<MarkdownContent> load) {
    ActivityExecutor executorService = controller.getExecutorService();
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();
    load.forEach(t -> {
        CompletableFuture<Pair<String, String>> completableFuture = CompletableFuture.supplyAsync(() -> {
          if (t.hasFile()) {
            return parser.parse(t.getMarkdownFile());
          } else {
            String desc = t.getMarkdown();
            if (desc == null || desc.trim().isEmpty()) {
              return "";
            } else {
              return parser.parse(desc, Optional.ofNullable(this.file.getValue()));
            }
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
      }

    );
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

  public WebView getWebView() {
    return webView;
  }

  public File getFile() {
    return file.get();
  }

  public SimpleObjectProperty<File> fileProperty() {
    return file;
  }

  public void setFile(File file) {
    this.file.set(file);
  }
}
