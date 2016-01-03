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
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class TextPreview implements Initializable {
  private StackPane stackPane;
  @Inject
  ActivityJavaFXExecutor javaFXExecutor;
  @Inject
  ActivityExecutor executor;
  private final AtomicReference<WebView> webView = new AtomicReference<>();
  private final AtomicReference<Path> currentPreview = new AtomicReference<>();

  private final ConcurrentHashMap<Path, Future<Path>> preloaded = new ConcurrentHashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    stackPane = new StackPane();
    javaFXExecutor.submit(() -> {
      webView.set(new WebView());
      stackPane.getChildren().add(webView.get());
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

  public void preload(Path temporarySourceFile, Path targetDirectory, String content) {
//    PreviewTask previewTask = new PreviewTask(temporarySourceFile, targetDirectory, content);
//    Future<Path> future = executor.submit(previewTask);
  }

  public void show(Path temporarySourceFile) {
    WebView webView = this.webView.get();
//    webView.getEngine().load(temporarySourceFile.);
  }

  public StackPane getRoot() {
    return stackPane;
  }

  public WebView getWebView() {
    return webView.get();
  }
}
