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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileExtensionValidator implements Validator<String> {

  private final List<String> fileExtensions;

  public FileExtensionValidator(String... extensions) {
    fileExtensions = Arrays.asList(extensions).stream().map(ext -> ext.startsWith(".") ? ext : "." + ext).collect(Collectors.toList());
  }

  @Override
  public ValidationResult apply(Control control, String s) {
    String validationMsg = Localized.get("validation.file.extension", fileExtensions);
    ValidationResult validationResult = ValidationResult.fromError(control, validationMsg);

    if (s == null || s.isEmpty()) {
      return validationResult;
    } else {
      int index = s.lastIndexOf(".");
      if (index < 0) {
        return validationResult;
      }
      String end = s.substring(index);
      if (!fileExtensions.contains(end)) {
        return validationResult;
      }
    }
    return null;
  }
}
