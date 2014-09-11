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
import org.controlsfx.validation.Validator;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class ValidFilePathValidator implements Validator<String> {
  @Override
  public ValidationResult apply(Control control, String s) {
    String validationMsg = Localized.get("validation.invalidpath", s);
    ValidationResult validationResult = ValidationResult.fromError(control, validationMsg);

    if (s == null || s.isEmpty()) {
      return validationResult;
    } else {
      try {
        Paths.get(s);
        File file = new File(s);
        file.toPath().toAbsolutePath();
        file.getCanonicalPath();
        file.exists();
        if (file.getParentFile() == null) {
          return validationResult;
        }
      } catch (InvalidPathException | IOException e) {
        return validationResult;
      }
    }
    return null;
  }
}
