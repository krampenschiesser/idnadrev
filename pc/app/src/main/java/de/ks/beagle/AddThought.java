package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.menu.MenuItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 */
@MenuItem("/main/thought")
public class AddThought extends VBox {
  private static final Logger log = LogManager.getLogger(AddThought.class);

  public AddThought() {
    TextField textField = new TextField("Yeah!");
    textField.setOnInputMethodTextChanged((InputMethodEvent e) -> log.info("received on action: {}", textField.getText()));
    textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean focused) {
        if (!focused) {
          Window window = textField.getScene().getWindow();
          if (window.isFocused() && window.isShowing()) {
            log.info("received on focus change {}", textField.getText());
          }
        }
      }
    });
    getChildren().addAll(textField, new TextField());
  }
}
