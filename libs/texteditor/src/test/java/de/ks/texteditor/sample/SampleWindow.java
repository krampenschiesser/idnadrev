/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.texteditor.sample;

import com.google.common.base.StandardSystemProperty;
import de.ks.standbein.application.MainWindow;
import de.ks.standbein.application.fxml.DefaultLoader;
import de.ks.texteditor.TextEditor;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class SampleWindow extends MainWindow {
  private static final Logger log = LoggerFactory.getLogger(SampleWindow.class);
  @Inject
  DefaultLoader<StackPane, TextEditor> loader;

  @Override
  public Parent getNode() {
    StackPane view = loader.load(TextEditor.class).getView();
    TextEditor editor = loader.getController();
    try {
      URL resource = getClass().getResource("sampledoc.adoc");
      Path path = Paths.get(resource.toURI());
      String content = Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
      editor.getCodeArea().replaceText(0, 0, content);

      Path sourceFile = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value(), "test.adoc");
      Path targetFile = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value(), "test.html");

      editor.setRenderingPaths(sourceFile, targetFile);
    } catch (Exception e) {
      log.error("Could not load sampledoc", e);
    }
    return view;
  }
}
