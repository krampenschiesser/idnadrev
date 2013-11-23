package de.ks.editor;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.eventsystem.bus.EventBus;
import de.ks.reflection.PropertyPath;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
  protected PropertyPath<?> path;
  protected TextField textField;
  @Inject
  protected EventBus bus;

  @Override
  public TextField getNode() {
    textField = new TextField();
    textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasFocused, Boolean isFocused) {
        Window window = textField.getScene().getWindow();
        if (window != null && window.isShowing() && window.isFocused() && !isFocused && wasFocused) {
          postValidationRequiredEvent();
        }
      }
    });
    return textField;
  }

  protected void postValidationRequiredEvent() {
//    ValidationRequiredEvent event = new ValidationRequiredEvent(path, textField.getText());
//    bus.post(event);
  }


//  public <T> T initialize(Class<T> model) {
//    path = new PropertyPath<>(model);
//    return (T) path.build();
//  }
//
//  public PropertyPath<?> getPath() {
//    return path;
//  }

  @Override
  public void forField(Field field) {
    super.forField(field);

  }
}
