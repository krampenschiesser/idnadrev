/*
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

package de.ks.workflow.view.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.bus.EventBus;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.i18n.Localized;
import de.ks.workflow.navigation.WorkflowNavigator;
import de.ks.workflow.step.DefaultOutput;
import de.ks.workflow.step.WorkflowStepConfig;
import de.ks.workflow.validation.event.ValidationResultEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class HorizontalNextPreviousView implements Initializable {
  @FXML
  protected Button previous;
  @FXML
  protected Button next;
  @FXML
  protected Button cancel;
  @FXML
  protected Label progressLabel;
  @FXML
  protected ProgressBar progressBar;

  @Inject
  protected WorkflowNavigator navigator;
  @Inject
  protected EventBus eventBus;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    next.setOnAction((event) -> navigator.next());
    previous.setOnAction((event) -> navigator.previous());
    cancel.setOnAction((event) -> navigator.cancel());
    progressBar.setVisible(false);
    progressLabel.setVisible(false);
    eventBus.register(this);
    navigator.currentStepProperty().addListener(new ChangeListener<WorkflowStepConfig>() {
      @Override
      public void changed(ObservableValue<? extends WorkflowStepConfig> observableValue, WorkflowStepConfig old, WorkflowStepConfig current) {
        if (current != null) {
          if (DefaultOutput.ROOT.name().equals(current.getIncomingKey())) {
            previous.setVisible(false);
          } else {
            previous.setVisible(true);
          }
          if (current.getOutputs().size() == 1) {
            if (current.getOutputs().iterator().next().getOutputs().isEmpty()) {
              next.setText(Localized.get("workflow.ok"));
            }
          }
        }
      }
    });
  }

  public Button getPrevious() {
    return previous;
  }

  public Button getNext() {
    return next;
  }

  public Button getCancel() {
    return cancel;
  }

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void onValidation(ValidationResultEvent e) {
    if (e.isSuccessful()) {
      next.setDisable(false);
    } else {
      next.setDisable(true);
    }
  }
}
