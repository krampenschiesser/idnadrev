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
package de.ks.flatjsondb.validator;

import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatjsondb.PersistentWork;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.validation.ValidationResult;
import de.ks.standbein.validation.Validator;
import javafx.scene.control.Control;

public class NamedEntityValidator implements Validator<Control, String> {
  private final Class<? extends NamedEntity> clazz;
  private final PersistentWork persistentWork;
  private final Localized localized;

  public NamedEntityValidator(Class<? extends NamedEntity> clazz, PersistentWork persistentWork, Localized localized) {
    this.clazz = clazz;
    this.persistentWork = persistentWork;
    this.localized = localized;
  }

  @Override
  public ValidationResult apply(Control control, String name) {
    if (name == null) {
      return null;
    } else if (name.trim().isEmpty()) {
      return null;
    } else {
      NamedEntity found = persistentWork.byName(clazz, name.trim());
      if (found == null) {
        String validationMsg = localized.get("validation.namedEntity.notFound", name.trim());
        return ValidationResult.createError(validationMsg);
      }
      return null;
    }
  }
}
