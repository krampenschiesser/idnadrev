package de.ks.editor;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.eventsystem.bus.EventBus;
import de.ks.workflow.validation.event.ValidationRequiredEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.lang.reflect.Field;

/**
 *
 */
@Default
@EditorFor(String.class)
public class StringEditor extends AbstractEditor {
  protected TextField textField;
  @Inject
  protected EventBus bus;

  @Override
  public TextField getNode() {
    if (textField == null) {
      textField = new TextField();
      textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasFocused, Boolean isFocused) {
          Scene scene = textField.getScene();
          if (scene != null) {
            Window window = scene.getWindow();
            if (window != null && window.isShowing() && window.isFocused()) {
              onFocusChange(wasFocused, isFocused);
            }
          }
        }
      });
      textField.textProperty().addListener(new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
          postValidationRequiredEvent();
        }
      });
    }
    return textField;
  }

  public void onFocusChange(boolean wasFocused, boolean focused) {
    postValidationRequiredEvent();
  }

  protected void postValidationRequiredEvent() {
    ValidationRequiredEvent event = new ValidationRequiredEvent(field, textField.textProperty().getValue());
    bus.post(event);
  }

  @Override
  public void forField(Field field) {
    super.forField(field);
  }
}
