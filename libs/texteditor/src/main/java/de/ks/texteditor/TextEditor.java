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

import de.ks.executor.group.LastExecutionGroup;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.activity.executor.ActivityJavaFXExecutor;
import de.ks.standbein.activity.initialization.ActivityInitialization;
import de.ks.texteditor.markup.Line;
import de.ks.texteditor.markup.MarkupStyleRange;
import de.ks.texteditor.markup.adoc.AdocMarkup;
import de.ks.texteditor.preview.TextPreview;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
  private StackPane previewContainer;
  @FXML
  private Tab htmlTab;
  @FXML
  private TextArea htmlEditor;

  @Inject
  ActivityJavaFXExecutor javaFXExecutorService;
  @Inject
  ActivityExecutor executorService;
  @Inject
  ActivityInitialization initialization;

  LineParser lineParser = new LineParser();
  private CodeArea codeArea;
  private TextPreview preview;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    codeArea = new CodeArea();
    IntFunction<Node> factory = LineNumberFactory.get(codeArea);
    codeArea.setParagraphGraphicFactory(factory);
    editorContainer.getChildren().add(codeArea);

    preview = initialization.loadAdditionalController(TextPreview.class).getController();
    previewContainer.getChildren().add(preview.getRoot());
    focusEditor();

    LastExecutionGroup<RichTextChange<Collection<String>>> markupGeneration = new LastExecutionGroup<>("MarkupGeneration", 300, executorService);
    final AtomicBoolean richtTextFXMurksGuard = new AtomicBoolean();//needed because sometimes richtextfx is stuck in a loop/stackoverflow (when replacing styles)
    codeArea.richChanges().subscribe(change -> {
      if (!richtTextFXMurksGuard.get()) {
        CompletableFuture<RichTextChange<Collection<String>>> future = markupGeneration.schedule(() -> change);
        if (future.getNumberOfDependents() == 0) {
          addChangeListener(future)//
            .thenRunAsync(() -> richtTextFXMurksGuard.set(false), javaFXExecutorService);
        }
      }
      richtTextFXMurksGuard.set(true);
    });
  }

  private CompletableFuture<Void> addChangeListener(CompletableFuture<RichTextChange<Collection<String>>> future) {

    return future.thenApplyAsync(change1 -> {
      List<Line> lines = lineParser.getLines(codeArea.getText());
      List<MarkupStyleRange> styleRanges = getMarkup().getStyleRanges(lines);
      return styleRanges;
    }, executorService)//
      .thenAcceptAsync(styleRanges -> {
        styleRanges.forEach(style -> codeArea.setStyle(style.getFromPos(), style.getToPos(), Collections.singleton(style.getStyleClass())));
      }, javaFXExecutorService);
  }

  protected AdocMarkup getMarkup() {
    return new AdocMarkup();
  }

  protected void focusEditor() {
    executorService.submit(() -> {
      try {
        Thread.sleep(100);
        javaFXExecutorService.submit(() -> codeArea.requestFocus());
      } catch (InterruptedException e) {
        //ok
      }
    });
  }

  public CodeArea getCodeArea() {
    return codeArea;
  }

  public String getContent() {
    return codeArea.getText();
  }

  public void setContent(String content) {
    codeArea.replaceText(content);
  }
}
