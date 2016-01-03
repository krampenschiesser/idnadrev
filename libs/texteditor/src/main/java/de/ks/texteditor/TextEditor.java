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
package de.ks.texteditor;

import de.ks.executor.JavaFXExecutorService;
import de.ks.executor.group.LastExecutionGroup;
import de.ks.texteditor.markup.Markup;
import de.ks.texteditor.markup.adoc.AdocMarkup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.RichTextChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

public class TextEditor implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(TextEditor.class);

  @FXML
  private TextField searchField;
  @FXML
  private HBox actionBar;
  @FXML
  private StackPane editorContainer;
  @FXML
  private Tab previewTab;
  @FXML
  private WebView preview;
  @FXML
  private Tab htmlTab;
  @FXML
  private TextArea htmlEditor;

  @Inject
  JavaFXExecutorService javaFXExecutorService;
  @Inject
  ExecutorService executorService;
  LineParser lineParser = new LineParser();
  private CodeArea codeArea;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    codeArea = new CodeArea();
    IntFunction<Node> factory = LineNumberFactory.get(codeArea);
    codeArea.setParagraphGraphicFactory(factory);
    editorContainer.getChildren().add(codeArea);

    executorService.submit(codeArea::requestFocus);


    LastExecutionGroup<RichTextChange<Collection<String>>> markupGeneration = new LastExecutionGroup<>("MarkupGeneration", 300, executorService);

    final AtomicBoolean richtTextFXMurksGuard = new AtomicBoolean();
    codeArea.richChanges().subscribe(change -> {
      if (!richtTextFXMurksGuard.get()) {
        CompletableFuture<RichTextChange<Collection<String>>> future = markupGeneration.schedule(() -> change);
        if (future.getNumberOfDependents() == 0) {
          future.thenApply(change1 -> {
            List<Markup.Line> lines = lineParser.getLines(codeArea.getText());
            List<Markup.MarkupStyleRange> styleRanges = new AdocMarkup().getStyleRanges(lines);
            return styleRanges;
//          }).thenApplyAsync(styleRanges -> {
//            codeArea.clearStyle(0, codeArea.getText().length());
//            return styleRanges;
//          }, javaFXExecutorService).thenAcceptAsync(styleRanges -> {
          }).thenAcceptAsync(styleRanges -> {
            styleRanges.forEach(style -> codeArea.setStyle(style.getFromPos(), style.getToPos(), Collections.singleton(style.getStyleClass())));
            richtTextFXMurksGuard.set(false);
          }, javaFXExecutorService);
        }
      }
      richtTextFXMurksGuard.set(true);
    });
  }

  public CodeArea getCodeArea() {
    return codeArea;
  }
}
