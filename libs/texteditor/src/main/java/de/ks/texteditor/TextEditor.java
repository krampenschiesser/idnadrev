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
import de.ks.standbein.application.fxml.DefaultLoader;
import de.ks.standbein.i18n.Localized;
import de.ks.texteditor.launch.search.SearchResult;
import de.ks.texteditor.launch.search.Searcher;
import de.ks.texteditor.markup.Line;
import de.ks.texteditor.markup.MarkupStyleRange;
import de.ks.texteditor.markup.adoc.AdocMarkup;
import de.ks.texteditor.preview.TextPreview;
import de.ks.texteditor.render.RenderType;
import de.ks.texteditor.render.Renderer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.RichTextChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class TextEditor implements Initializable {
  public static CompletableFuture<DefaultLoader<Node, TextEditor>> load(ActivityInitialization initialization, Consumer<StackPane> viewConsumer, Consumer<TextEditor> controllerConsumer) {
    return initialization.loadAdditionalControllerWithFuture(TextEditor.class)//
      .thenApply(loader -> {
        viewConsumer.accept((StackPane) loader.getView());
        controllerConsumer.accept(loader.getController());
        return loader;
      });
  }

  private static final Logger log = LoggerFactory.getLogger(TextEditor.class);
  @FXML
  StackPane root;
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
  @FXML
  protected HBox exportActions;

  protected final SimpleStringProperty text = new SimpleStringProperty("");
  protected final ObservableList<SearchResult> searchResults = FXCollections.observableArrayList();

  @Inject
  ActivityJavaFXExecutor javaFXExecutorService;
  @Inject
  ActivityExecutor executorService;
  @Inject
  ActivityInitialization initialization;
  @Inject
  Localized localized;
  @Inject
  Searcher searcher;

  LineParser lineParser = new LineParser();
  CodeArea codeArea;
  TextPreview preview;

  Path sourcePath;
  Path targetPath;
  Path lastRenderingTarget;

  String lastSearchText;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    codeArea = new CodeArea();
    IntFunction<Node> factory = LineNumberFactory.get(codeArea);
    codeArea.setParagraphGraphicFactory(factory);
    editorContainer.getChildren().add(codeArea);

    preview = initialization.loadAdditionalController(TextPreview.class).getController();
    previewContainer.getChildren().add(preview.getRoot());
    htmlEditor.textProperty().bind(preview.htmlProperty());
    focusEditor();

    LastExecutionGroup<RichTextChange<Collection<String>>> markupGeneration = new LastExecutionGroup<>("MarkupGeneration", 300, executorService);
    final AtomicBoolean richtTextFXMurksGuardMarkup = new AtomicBoolean();//needed because sometimes richtextfx is stuck in a loop/stackoverflow (when replacing styles)
    codeArea.richChanges().subscribe(change -> {
      if (!richtTextFXMurksGuardMarkup.get()) {
        CompletableFuture<RichTextChange<Collection<String>>> future = markupGeneration.schedule(() -> change);
        if (future.getNumberOfDependents() == 0) {
          addMarkupChangeListener(future)//
            .thenRunAsync(() -> richtTextFXMurksGuardMarkup.set(false), javaFXExecutorService);
        }
      }
      richtTextFXMurksGuardMarkup.set(true);
    });

    final AtomicBoolean richtTextFXMurksGuardPreview = new AtomicBoolean();//needed because sometimes richtextfx is stuck in a loop/stackoverflow (when replacing styles)
    LastExecutionGroup<RichTextChange<Collection<String>>> previewGeneration = new LastExecutionGroup<>("PreviewGeneration", 1000, executorService);
    codeArea.richChanges().subscribe(change -> {
      if (!richtTextFXMurksGuardPreview.get()) {
        richtTextFXMurksGuardPreview.set(true);
        research(searchField.textProperty().getValueSafe());
        CompletableFuture<RichTextChange<Collection<String>>> future = previewGeneration.schedule(() -> change);
        if (future.getNumberOfDependents() == 0) {
          addPreviewChangeListener(future)//
            .thenRunAsync(() -> richtTextFXMurksGuardPreview.set(false), javaFXExecutorService);
        }
      }
    });

    codeArea.textProperty().addListener((p, o, n) -> {
      if (n != null && !n.equals(text.get())) {
        text.set(n);
      }
    });
    text.addListener((p, o, n) -> {
      if (n != null && !n.equals(codeArea.getText())) {
        codeArea.replaceText(n);
      }
    });
    recreateRenderingButtons();
    preview.currentRendererProperty().addListener((p, o, n) -> {
      if (n != null && !n.equals(o)) {
        recreateRenderingButtons();
      }
    });
    root.setOnKeyPressed(e -> {
      if (e.getCode() == KeyCode.F && e.isControlDown()) {
        searchField.requestFocus();
      }
    });
    searchField.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        searchField.setText("");
        resetSearchResults();
      } else if (e.getCode() == KeyCode.ENTER) {
        String currentSearchText = searchField.textProperty().getValueSafe();
        if (currentSearchText.equals(lastSearchText) && !searchResults.isEmpty()) {
          int caretPosition = codeArea.getCaretPosition();
          Predicate<SearchResult> filter;
          boolean searchUp = e.isShiftDown();
          if (searchUp) {
            filter = r -> r.getEnd() < caretPosition;
          } else {
            filter = r -> r.getStart() >= caretPosition;
          }
          List<SearchResult> resultsToSearch = new ArrayList<SearchResult>(this.searchResults);
          if (searchUp) {
            Collections.reverse(resultsToSearch);
          }
          SearchResult result = resultsToSearch.stream().sequential().filter(filter).findFirst().orElse(searchUp ? this.searchResults.get(this.searchResults.size() - 1) : this.searchResults.get(0));
          codeArea.positionCaret(result.getStart());
          codeArea.selectRange(result.getStart(), result.getEnd());
        } else {
          research(currentSearchText);
        }

      }
    });
    searchResults.addListener((ListChangeListener<SearchResult>) c -> {
      CompletableFuture<RichTextChange<Collection<String>>> cf = CompletableFuture.completedFuture(null);
      addMarkupChangeListener(cf);
    });
  }

  protected void research(String currentSearchText) {
    resetSearchResults();
    lastSearchText = currentSearchText;
    CompletableFuture.supplyAsync(() -> searcher.search(searchField.getText(), codeArea.getText()), executorService)//
      .thenAcceptAsync(results -> {
        this.searchResults.clear();
        this.searchResults.addAll(results);
        if (!results.isEmpty() && searchField.isFocused()) {
          codeArea.positionCaret(results.get(0).getStart());
          codeArea.selectRange(results.get(0).getStart(), results.get(0).getEnd());
        }
      }, javaFXExecutorService);
  }

  protected void resetSearchResults() {
    for (SearchResult searchResult : new ArrayList<>(searchResults)) {
      codeArea.clearStyle(searchResult.getStart(), searchResult.getEnd());
    }
    searchResults.clear();
  }

  private void recreateRenderingButtons() {
    exportActions.getChildren().clear();
    List<RenderType> supportedRenderingTypes = preview.getRenderer().getSupportedRenderingTypes();
    for (RenderType renderType : supportedRenderingTypes) {
      String typeName = renderType.name().toLowerCase(Locale.ROOT);
      typeName = typeName.substring(0, 1).toUpperCase(Locale.ROOT) + typeName.substring(1);
      Button renderButton = new Button(localized.get("texteditor.renderTo", typeName));
      renderButton.setOnAction(event -> {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(localized.get("texteditor.chooseFile"));
        if (sourcePath != null && lastRenderingTarget == null) {
          lastRenderingTarget = sourcePath;
        }
        if (lastRenderingTarget != null) {
          Path source = lastRenderingTarget;
          fileChooser.setInitialDirectory(source.getParent().toFile());
          String fileName = source.getFileName().toString();
          if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
          }
          fileName += renderType.getFileExtension();
          fileChooser.setInitialFileName(fileName);
        }
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(renderType.name(), renderType.getFileExtension()));
        File file = fileChooser.showSaveDialog(htmlEditor.getScene().getWindow());
        if (file != null) {
          lastRenderingTarget = file.toPath();

          Renderer renderer = preview.getRenderer();
          try {
            Files.write(sourcePath, codeArea.getText().getBytes(StandardCharsets.UTF_8));
          } catch (IOException e) {
            log.error("Could not render to {}", file, e);
          }
          renderer.renderFilePreview(sourcePath, file.toPath(), renderType);
        }
      });
      exportActions.getChildren().add(renderButton);
    }
  }

  private CompletableFuture<Void> addPreviewChangeListener(CompletableFuture<RichTextChange<Collection<String>>> future) {
    return future.thenAcceptAsync(change -> {
      preview.preload(sourcePath, targetPath, codeArea.getText());
    }, executorService).thenRunAsync(() -> {
      preview.show(sourcePath);
    }, javaFXExecutorService);
  }

  private CompletableFuture<Void> addMarkupChangeListener(CompletableFuture<RichTextChange<Collection<String>>> future) {
    return future.thenApplyAsync(change1 -> {
      List<Line> lines = lineParser.getLines(codeArea.getText());
      List<MarkupStyleRange> styleRanges = getMarkup().getStyleRanges(lines);
      return styleRanges;
    }, executorService).thenAcceptAsync(styleRanges -> {
      styleRanges.forEach(style -> codeArea.setStyle(style.getFromPos(), style.getToPos(), Collections.singleton(style.getStyleClass())));
      for (SearchResult result : searchResults) {
        codeArea.setStyle(result.getStart(), result.getEnd(), Collections.singleton("searchresult"));
      }
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

  public TextPreview getPreview() {
    return preview;
  }

  public void setRenderingPaths(Path sourcePath, Path targetPath) {
    if (Files.isDirectory(sourcePath) || Files.isDirectory(targetPath)) {
      throw new IllegalArgumentException("Paths have to be a file");
    }
    this.sourcePath = sourcePath;
    this.targetPath = targetPath;
  }

  public Path getSourcePath() {
    return sourcePath;
  }

  public Path getTargetPath() {
    return targetPath;
  }

  public String getText() {
    return text.getValueSafe();
  }

  public SimpleStringProperty textProperty() {
    return text;
  }

  public void setText(String text) {
    this.text.set(text == null ? "" : text);
  }
}
