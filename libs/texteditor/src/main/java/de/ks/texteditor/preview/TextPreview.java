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
package de.ks.texteditor.preview;

import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.activity.executor.ActivityJavaFXExecutor;
import de.ks.texteditor.module.TextEditorModule;
import de.ks.texteditor.render.Renderer;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TextPreview implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(TextPreview.class);
  private StackPane stackPane;
  @Inject
  ActivityJavaFXExecutor javaFXExecutor;
  @Inject
  ActivityExecutor executor;
  private final AtomicReference<WebView> webView = new AtomicReference<>();
  private final AtomicReference<Path> currentPreview = new AtomicReference<>();
  protected final SimpleStringProperty html = new SimpleStringProperty();

  private final ConcurrentHashMap<Path, CompletableFuture<Path>> preloaded = new ConcurrentHashMap<>();
  @Inject
  private Set<Renderer> renderers;
  @Inject
  @Named(TextEditorModule.DEFAULT_RENDERER)
  private volatile String currentRenderer;
  protected final AtomicInteger lastScrollPos = new AtomicInteger();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    stackPane = new StackPane();
    javaFXExecutor.submit(() -> {
      WebView view = new WebView();
      webView.set(view);
      stackPane.getChildren().add(view);
      view.getEngine().getLoadWorker().stateProperty().addListener((p, o, n) -> {
        if (n == Worker.State.SUCCEEDED) {
          view.getEngine().executeScript("window.scrollTo(" + 0 + ", " + lastScrollPos.get() + ")");
        }
      });
    });

  }

  public void preload(String id, String content) {
    Path path = getTemporaryFile(id);
    Path target = path.getParent();
    preload(path, target, content);

  }

  private Path getTemporaryFile(String id) {
    return null;
  }

  public void preload(Path temporarySourceFile, Path targetFile, String content) {
    Renderer renderer = renderers.stream().filter(r -> r.getName().equals(currentRenderer)).findFirst().get();
    CompletableFuture<Path> future = CompletableFuture.supplyAsync(new PreviewTask(temporarySourceFile, targetFile, content, renderer), executor);
    preloaded.put(temporarySourceFile, future);
  }

  public void show(Path temporarySourceFile) {
    WebView webView = this.webView.get();
    webView.getEngine().setOnAlert(e -> {
      log.info("Alert {}", e.getData());
    });
    CompletableFuture<Path> pathFuture = preloaded.get(temporarySourceFile);
    pathFuture.thenApplyAsync(path -> {
      try {
        String content = Files.readAllLines(path, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
        return content;
      } catch (IOException e) {
        return "";
      }
    }, executor).thenAcceptAsync(html::set, javaFXExecutor);

    pathFuture.thenAcceptAsync(path -> {
      try {
        int scrollpos = (Integer) webView.getEngine().executeScript("document.body.scrollTop");
        lastScrollPos.set(scrollpos);
        URL url = path.toUri().toURL();
        webView.getEngine().load(url.toExternalForm());
      } catch (MalformedURLException e) {
        //
      }
    }, javaFXExecutor);
  }

  public StackPane getRoot() {
    return stackPane;
  }

  public WebView getWebView() {
    return webView.get();
  }

  public String getHtml() {
    return html.get();
  }

  public ReadOnlyStringProperty htmlProperty() {
    return html;
  }

  public String getCurrentRenderer() {
    return currentRenderer;
  }

  public void setCurrentRenderer(String currentRenderer) {
    this.currentRenderer = currentRenderer;
  }
}
