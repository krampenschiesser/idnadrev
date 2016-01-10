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
package de.ks.idnadrev.task.finish;

import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.standbein.BaseController;
import de.ks.texteditor.TextEditor;
import de.ks.texteditor.preview.TextPreview;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class FinishTask extends BaseController<Task> {
  private static final Logger log = LoggerFactory.getLogger(FinishTask.class);
  @FXML
  private StackPane expectedOutcomeContainer;
  @FXML
  private StackPane finalOutcomeContainer;

  protected TextEditor finalOutcome;
  protected TextPreview expectedOutcome;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    TextEditor.load(this.activityInitialization, pane -> finalOutcomeContainer.getChildren().add(pane), editor -> {
      finalOutcome = editor;
    });
    TextPreview.load(this.activityInitialization, pane -> expectedOutcomeContainer.getChildren().add(pane), viewer -> expectedOutcome = viewer);
  }

  @Override
  public void duringLoad(Task model) {
    AdocFile adocFile = model.getAdocFile();
    expectedOutcome.preload(adocFile.getTmpFile(), adocFile.getRenderingPath(), adocFile.getContent());
    controller.getJavaFXExecutor().submit(() -> expectedOutcome.show(adocFile.getTmpFile()));

    String outcome = model.getOutcome().getFinalOutcome();
    controller.getJavaFXExecutor().submit(() -> {
      log.info("Setting final outcome text {}", outcome);
      finalOutcome.setText(outcome);
    });
  }

  @Override
  public void duringSave(Task model) {
    model.getOutcome().setFinalOutcome(finalOutcome.getText());
  }

  @FXML
  void onSave() {
    controller.save();
    controller.stopCurrent();
  }
}
