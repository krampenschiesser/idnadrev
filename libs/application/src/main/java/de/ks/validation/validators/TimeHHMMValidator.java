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

package de.ks.validation.validators;

import de.ks.validation.ValidationMessage;
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

public class TimeHHMMValidator implements Validator<String> {
  private static final Logger log = LoggerFactory.getLogger(TimeHHMMValidator.class);
  private final AtomicReference<LocalTime> time = new AtomicReference<>();

  @Override
  public ValidationResult apply(Control control, String timeString) {
    if (timeString.trim().isEmpty()) {
      return null;
    }
    if (timeString.contains(":")) {
      return parseFormat(control, timeString);
    }
    return ValidationResult.fromMessages(new ValidationMessage("validation.timeHHMM", control));
  }

  private ValidationResult parseFormat(Control control, String timeString) {
    ValidationResult result = ValidationResult.fromMessages(new ValidationMessage("validation.timeHHMM", control));

    String[] split = timeString.split(":");
    if (split.length != 2) {
      time.set(null);
      return result;
    }

    String hourString = split[0];
    String minutesString = split[1];
    if (minutesString.length() != 2 || hourString.length() != 2) {
      time.set(null);
      return result;
    }

    try {
      int hours = Integer.valueOf(hourString);
      int minutes = Integer.valueOf(minutesString);
      if (minutes > 59 || hours > 23) {
        time.set(null);
        return result;
      }
      time.set(LocalTime.of(hours, minutes));
    } catch (NumberFormatException e) {
      time.set(null);
      return result;
    }
    return null;
  }

  public LocalTime getTime() {
    return time.get();
  }
}
