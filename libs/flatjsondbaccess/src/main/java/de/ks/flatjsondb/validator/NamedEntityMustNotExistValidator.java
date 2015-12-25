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

import java.util.function.Predicate;

public class NamedEntityMustNotExistValidator<T extends NamedEntity> implements Validator<Control, String> {
  protected Class<T> entityClass;
  private final Predicate<T> ignore;
  private final PersistentWork persistentWork;
  private final Localized localized;

  public NamedEntityMustNotExistValidator(Class<T> entityClass, PersistentWork persistentWork, Localized localized) {
    this(entityClass, any -> false, persistentWork, localized);
  }

  public NamedEntityMustNotExistValidator(Class<T> entityClass, Predicate<T> ignore, PersistentWork persistentWork, Localized localized) {
    this.entityClass = entityClass;
    this.ignore = ignore;
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
      T found = persistentWork.byName(entityClass, name.trim());
      if (found != null) {
        if (ignore != null && ignore.test(found)) {
          return null;
        }
        String validationMsg = localized.get("validation.namedEntity.mustNotExist", name.trim());
        return ValidationResult.createError(validationMsg);
      }
      return null;
    }
  }
}
