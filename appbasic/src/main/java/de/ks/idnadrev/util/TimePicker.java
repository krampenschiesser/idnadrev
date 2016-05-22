/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.util;

import de.ks.standbein.i18n.Localized;
import de.ks.standbein.validation.ValidationRegistry;
import de.ks.standbein.validation.ValidationResult;
import de.ks.standbein.validation.Validator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import javax.inject.Inject;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class TimePicker implements Initializable {
  @FXML
  TextField timeEditor;

  @Inject
  ValidationRegistry validationRegistry;
  @Inject
  Localized localized;

  final SimpleObjectProperty<String> style = new SimpleObjectProperty<>("HH:mm");
  final SimpleObjectProperty<DateTimeFormatter> formatter = new SimpleObjectProperty<>(DateTimeFormatter.ofPattern(style.get()));
  final SimpleObjectProperty<LocalTime> time = new SimpleObjectProperty<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    style.addListener((p, o, n) -> {
      if (n != null) {
        formatter.set(DateTimeFormatter.ofPattern(style.get()));
      }
    });
    TimeValidator validator = new TimeValidator(localized, formatter, time);
    validationRegistry.registerValidator(timeEditor, validator);
  }

  public TextField getTimeEditor() {
    return timeEditor;
  }

  public TimePicker setTimeEditor(TextField timeEditor) {
    this.timeEditor = timeEditor;
    return this;
  }

  public LocalTime getTime() {
    return time.get();
  }

  public SimpleObjectProperty<LocalTime> timeProperty() {
    return time;
  }

  public void setTime(LocalTime time) {
    this.time.set(time);
  }

  public static class TimeValidator implements Validator<Control, String> {
    private final Localized localized;
    private final SimpleObjectProperty<DateTimeFormatter> formatter;
    private final SimpleObjectProperty<LocalTime> time;

    public TimeValidator(Localized localized, SimpleObjectProperty<DateTimeFormatter> formatter, SimpleObjectProperty<LocalTime> time) {
      this.localized = localized;
      this.formatter = formatter;
      this.time = time;
    }

    @Override
    public ValidationResult apply(Control control, String s) {
      if (s == null || s.trim().isEmpty()) {
        return null;
      }
      try {
        LocalTime result = LocalTime.parse(s, formatter.get());
        time.set(result);
        return null;
      } catch (DateTimeParseException e) {
        return ValidationResult.createError(localized.get("validation.time", s));
      }
    }
  }
}
