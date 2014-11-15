/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.information.chart.adoc;

import de.ks.activity.ActivityController;
import de.ks.activity.ActivityHint;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.information.view.selection.chart.ChartSelectionActivity;
import de.ks.idnadrev.information.view.selection.chart.ChartSelectionController;
import de.ks.text.command.AsciiDocEditorCommand;
import javafx.scene.control.TextArea;

import javax.inject.Inject;

public class InsertChart implements AsciiDocEditorCommand {
  @Inject
  ActivityController controller;
  ChartInfo selectedItem;

  @Override
  public void execute(TextArea editor) {
    int caretPosition = editor.getCaretPosition();

    ActivityHint activityHint = new ActivityHint(ChartSelectionActivity.class);
    activityHint.setRefreshOnReturn(false);
    activityHint.setReturnToActivity(controller.getCurrentActivityId());
    activityHint.setReturnToDatasourceHint(() -> {
      this.selectedItem = controller.getControllerInstance(ChartSelectionController.class).getSelectedItem();
      return null;
    });
    activityHint.setReturnToRunnable(() -> {
      controller.getJavaFXExecutor().submit(() -> {
        String insertText = getInsertText();
        int insertPosition = caretPosition;
        editor.insertText(insertPosition, insertText);
        editor.positionCaret(insertPosition + getNextCaretOffset());
        editor.requestFocus();
      });
    });
    controller.startOrResume(activityHint);
  }

  @Override
  public String getInsertText() {
    if (selectedItem != null) {
      return "chart::" + selectedItem.getId();
    } else {
      return "";
    }
  }
}
