/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.entity.cost;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Entity
public class BookingPattern extends NamedPersistentObject<BookingPattern> {
  @NotNull
  protected String regex;
  protected transient Pattern compiledPattern;
  @NotNull
  protected String category;
  protected boolean simpleContains;

  protected BookingPattern() {
  }

  public BookingPattern(String name) {
    super(name);
  }

  public String getRegex() {
    return regex;
  }

  public BookingPattern setRegex(String regex) {
    this.regex = regex;
    return this;
  }

  public Pattern getCompiledPattern() {
    if (compiledPattern == null) {
      compiledPattern = Pattern.compile(regex);
    }
    return compiledPattern;
  }

  public BookingPattern setCompiledPattern(Pattern compiledPattern) {
    this.compiledPattern = compiledPattern;
    return this;
  }

  public String getCategory() {
    return category;
  }

  public BookingPattern setCategory(String category) {
    this.category = category;
    return this;
  }

  public boolean isSimpleContains() {
    return simpleContains;
  }

  public BookingPattern setSimpleContains(boolean simpleContains) {
    this.simpleContains = simpleContains;
    return this;
  }

  public Optional<String> parse(String text) {
    if (simpleContains) {
      String regexLower = getRegex().toLowerCase(Locale.ROOT);
      text = text.toLowerCase(Locale.ROOT);
      String[] split = regexLower.split("\\,");
      for (String currentRegex : split) {
        if (text.contains(currentRegex)) {
          return Optional.of(getCategory());
        }
      }
      return Optional.empty();
    } else {
      if (getCompiledPattern().matcher(text).matches()) {
        return Optional.of(getCategory());
      } else {
        return Optional.empty();
      }
    }
  }
}
