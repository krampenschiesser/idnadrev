package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.bus.EventBus;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.workflow.validation.event.ValidationResultEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 */
public class EditStepGrid implements Initializable {
  @FXML
  protected GridPane editGrid;
  @FXML
  protected Label instruction;
  @FXML
  protected TextArea validationMessage;
  @Inject
  protected EventBus eventBus;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    eventBus.register(this);
    validationMessage.setVisible(false);
  }

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void onValidationEvent(ValidationResultEvent event) {
    if (event.isSuccessful()) {
      validationMessage.setVisible(false);
    } else {
      StringBuilder builder = new StringBuilder();
      Set<ConstraintViolation<Object>> violations = event.getViolations();
      for (ConstraintViolation<Object> violation : violations) {
        builder.append(violation.getMessage()).append("\n");
      }
      validationMessage.setText(builder.toString());
      validationMessage.setVisible(true);
    }
  }

  public GridPane getEditGrid() {
    return editGrid;
  }

  public Label getInstruction() {
    return instruction;
  }

  public TextArea getValidationMessage() {
    return validationMessage;
  }
}
