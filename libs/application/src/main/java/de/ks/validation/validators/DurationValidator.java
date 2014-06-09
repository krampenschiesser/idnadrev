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

import de.ks.i18n.Localized;
import de.ks.validation.ValidationMessage;
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DurationValidator implements Validator<String> {
  private static final Logger log = LoggerFactory.getLogger(DurationValidator.class);
  private final String minutesSuffixShort;
  private final String minutesSuffifx;
  private final String hoursSuffix;
  private final String hoursSuffixShort;
  private final String durationFormat;

  public DurationValidator() {
    minutesSuffifx = Localized.get("duration.minutes");
    minutesSuffixShort = Localized.get("duration.minutes.short");
    hoursSuffix = Localized.get("duration.hours");
    hoursSuffixShort = Localized.get("duration.hours.short");
    durationFormat = Localized.get("duration.format");
  }

  @Override
  public ValidationResult apply(Control control, String timeString) {
    if (timeString == null) {
      return null;
    } else if (timeString.trim().isEmpty()) {
      return null;
    } else {
      if (timeString.contains(":")) {
        return parseFormat(control, timeString);
      } else if (hasSuffix(timeString, minutesSuffixShort, minutesSuffifx)) {
        return null;
      } else if (hasSuffix(timeString, hoursSuffixShort, hoursSuffix)) {
        return null;
      }
      return ValidationResult.fromMessages(new ValidationMessage("validation.duration", control, minutesSuffifx, minutesSuffixShort, hoursSuffix, hoursSuffixShort, durationFormat));
    }
  }

  private ValidationResult parseFormat(Control control, String timeString) {
    ValidationResult result = ValidationResult.fromMessages(new ValidationMessage("validation.duration.invalidFormat", control, durationFormat));

    String[] split = timeString.split(":");
    if (split.length != 2) {
      return result;
    }

    String hourString = split[0];
    String minutesString = split[1];
    if (minutesString.length() < 2) {
      return result;
    }

    try {
      int hours = Integer.valueOf(hourString);
      int minutes = Integer.valueOf(minutesString);
      if (minutes > 59) {
        return result;
      }
    } catch (NumberFormatException e) {
      return result;
    }
    return null;
  }

  protected boolean hasSuffix(String timeString, String shortSuffix, String longSuffix) {
    int shortIndex = timeString.trim().indexOf(shortSuffix);
    int longIndex = timeString.trim().indexOf(longSuffix);

    if (longIndex >= 0) {
      timeString = timeString.substring(0, longIndex);
    } else if (shortIndex >= 0) {
      timeString = timeString.substring(0, shortIndex);
    }
    boolean hasTimeUnit = shortIndex >= 0 || longIndex >= 0;
    if (!hasTimeUnit) {
      return false;
    }
    try {
      Integer.parseInt(timeString.trim());
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  protected boolean isHours(String timeString) {
    return timeString.trim().endsWith(hoursSuffixShort) || timeString.trim().endsWith(hoursSuffix);
  }
}
