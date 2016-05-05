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
import de.ks.standbein.activity.initialization.ActivityInitialization;
import de.ks.standbein.application.fxml.DefaultLoader;
import de.ks.standbein.i18n.Localized;
import de.ks.texteditor.module.TextEditorModule;
import de.ks.texteditor.render.Renderer;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TextPreview implements Initializable {

  public static CompletableFuture<DefaultLoader<Node, TextPreview>> load(ActivityInitialization initialization, Consumer<StackPane> viewConsumer, Consumer<TextPreview> controllerConsumer) {
    return initialization.loadAdditionalControllerWithFuture(TextPreview.class)//
      .thenApply(loader -> {
        TextPreview controller = loader.getController();
        controllerConsumer.accept(controller);
        viewConsumer.accept(controller.getRoot());
        return loader;
      });
  }

  private static final Logger log = LoggerFactory.getLogger(TextPreview.class);

  StackPane stackPane;
  final AtomicReference<WebView> webView = new AtomicReference<>();
  final SimpleStringProperty html = new SimpleStringProperty();
  final AtomicInteger lastScrollPos = new AtomicInteger();

  final ActivityJavaFXExecutor javaFXExecutor;
  final ActivityExecutor executor;
  final Set<Renderer> renderers;
  final SimpleStringProperty currentRenderer = new SimpleStringProperty();

  final ConcurrentHashMap<Path, CompletableFuture<Path>> preloaded = new ConcurrentHashMap<>();
  @Inject
  Localized localized;

  //used for rendering when no path is given
  private Path tmpDst;
  private Path tmpSrc;
  private volatile Path requestedShowPath;

  @Inject
  public TextPreview(@Named(TextEditorModule.DEFAULT_RENDERER) String currentRenderer, Set<Renderer> renderers, ActivityJavaFXExecutor javaFXExecutor, ActivityExecutor executor) {
    this.javaFXExecutor = javaFXExecutor;
    this.executor = executor;
    this.currentRenderer.set(currentRenderer);
    this.renderers = renderers;
    try {
      if (tmpSrc == null) {
        tmpSrc = Files.createTempFile("renderSrc", ".adoc");
        tmpSrc.toFile().deleteOnExit();
      }
      if (tmpDst == null) {
        tmpDst = Files.createTempFile("renderTarget", ".html");
        tmpDst.toFile().deleteOnExit();
      }
    } catch (IOException e) {
      log.info("Could not create rendering target and source ", e);
    }
  }

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

  public void preload(Path adocFile, Path renderTarget) {
    if (adocFile == null) {
      return;
    }
    Renderer renderer = getRenderer();
    CompletableFuture<Path> future = CompletableFuture.supplyAsync(new ViewTask(adocFile, renderTarget, renderer), executor);
    preloaded.put(adocFile, future);
    if (requestedShowPath != null && requestedShowPath.equals(adocFile)) {
      future.thenAcceptAsync(path -> {
        show(requestedShowPath);
      }, javaFXExecutor);
    }
  }

  public void preload(Path temporarySourceFile, Path targetFile, String content) {
    if (temporarySourceFile == null && tmpSrc != null) {
      temporarySourceFile = tmpSrc;
    }
    if (targetFile == null && tmpDst != null) {
      targetFile = tmpDst;
    }
    Renderer renderer = getRenderer();
    CompletableFuture<Path> future = CompletableFuture.supplyAsync(new PreviewTask(temporarySourceFile, targetFile, content, renderer), executor);
    preloaded.put(temporarySourceFile, future);
    if (requestedShowPath != null && requestedShowPath.equals(temporarySourceFile)) {
      future.thenAcceptAsync(path -> {
        show(requestedShowPath);
      }, javaFXExecutor);
    }
  }

  public Renderer getRenderer() {
    return renderers.stream().filter(r -> r.getName().equals(currentRenderer.get())).findFirst().get();
  }

  public void show(Path temporarySourceFile) {
    if (temporarySourceFile == null) {
      temporarySourceFile = tmpSrc;
    }
    WebView webView = this.webView.get();
    webView.getEngine().setOnAlert(e -> {
      log.info("Alert {}", e.getData());
    });
    CompletableFuture<Path> pathFuture = preloaded.get(temporarySourceFile);
    if (pathFuture != null) {
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
    } else {
      requestedShowPath = temporarySourceFile;
    }
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
    return currentRenderer.get();
  }

  public void setCurrentRenderer(String currentRenderer) {
    this.currentRenderer.set(currentRenderer);
  }

  public SimpleStringProperty currentRendererProperty() {
    return currentRenderer;
  }

  public void clear() {
    preloaded.clear();
    clearContent();
  }

  public void clearContent() {
    webView.get().getEngine().loadContent("");
  }

}
