/*
 * Copyright [2015] [Christian Loehnert]
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

package de.ks.idnadrev.cost.csvimport;

import de.ks.standbein.i18n.Localized;
import de.ks.standbein.validation.ValidationMessage;
import de.ks.standbein.validation.ValidationResult;
import de.ks.standbein.validation.Validator;
import javafx.scene.control.Control;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.OptionalInt;

public class AmountColumnValidator implements Validator<Control, String> {
  protected final Localized localized;

  @Inject
  public AmountColumnValidator(Localized localized) {
    this.localized = localized;
  }

  @Override
  public ValidationResult apply(Control control, String s) {
    String[] split = s.split("\\,");
    try {
      OptionalInt first = Arrays.asList(split).stream().mapToInt(val -> Integer.valueOf(val)).filter(i -> i < 0).findFirst();
      if (first.isPresent()) {
        return new ValidationResult().add(new ValidationMessage(localized.get("validation.number.greaterEquals", 0)));
      }
      first = Arrays.asList(split).stream().mapToInt(val -> Integer.valueOf(val)).filter(i -> i > 100).findFirst();
      if (first.isPresent()) {
        return new ValidationResult().add(new ValidationMessage(localized.get("validation.number.lessThan", 100)));
      }
    } catch (NumberFormatException e) {
      return new ValidationResult().add(new ValidationMessage(localized.get("validation.mustBeInteger")));
    }
    return null;
  }
}
