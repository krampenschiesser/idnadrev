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
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;

public class IntegerRangeValidator extends IntegerValidator {
  protected final int minInclusive;
  protected final int maxExclusive;

  public IntegerRangeValidator(int minInclusive, int maxExclusive) {
    this.minInclusive = minInclusive;
    this.maxExclusive = maxExclusive;
  }

  @Override
  protected ValidationResult furtherProcessing(Control control, Integer value) {
    if (value < minInclusive) {
      String validationMsg = Localized.get("validation.number.greaterEquals", minInclusive);
      return ValidationResult.fromError(control, validationMsg);
    } else if (value >= maxExclusive) {
      String validationMsg = Localized.get("validation.number.lessThan", maxExclusive);
      return ValidationResult.fromError(control, validationMsg);
    }
    return super.furtherProcessing(control, value);
  }
}
