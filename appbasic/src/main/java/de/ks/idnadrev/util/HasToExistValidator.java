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

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.index.StandardQueries;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.validation.ValidationResult;
import de.ks.standbein.validation.Validator;
import javafx.scene.control.Control;

import java.util.Collection;
import java.util.Locale;

public class HasToExistValidator implements Validator<Control, String> {
  private final Index index;
  private final Localized localized;

  public HasToExistValidator(Index index, Localized localized) {
    this.index = index;
    this.localized = localized;
  }

  @Override
  public ValidationResult apply(Control control, String s) {
    if (s != null && !s.trim().isEmpty()) {
      Collection<AdocFile> files = index.queryNonNull(AdocFile.class, StandardQueries.titleQuery(), title -> title.toLowerCase(Locale.ROOT).trim().equals(s.toLowerCase(Locale.ROOT).trim()));
      if (files.isEmpty()) {
        return ValidationResult.createError(localized.get("adocfile.has.to.exist", s));
      }
    }
    return null;
  }
}
