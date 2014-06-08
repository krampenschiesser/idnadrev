/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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
package de.ks.validation;

import de.ks.i18n.Localized;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.NamedPersistentObject;
import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

public class NamedEntityValidator implements Validator<String> {
  protected Class<? extends NamedPersistentObject> entityClass;

  public NamedEntityValidator(Class<? extends NamedPersistentObject> entityClass) {
    this.entityClass = entityClass;
  }

  @Override
  public ValidationResult apply(Control control, String name) {
    if (name == null) {
      return null;
    } else if (name.isEmpty()) {
      return null;
    } else {
      NamedPersistentObject found = PersistentWork.forName(entityClass, name);
      if (found == null) {
        String validationMsg = Localized.get("validation.namedEntity.notFound", name);
        return ValidationResult.fromError(control, validationMsg);
      }
      return null;
    }
  }
}
