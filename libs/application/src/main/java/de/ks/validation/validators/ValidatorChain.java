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

import javafx.scene.control.Control;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidatorChain<T> implements Validator<T> {
  protected final List<Validator<T>> delegates = new LinkedList<>();

  @Override
  public ValidationResult apply(Control control, T t) {
    List<ValidationResult> results = delegates.stream().map(d -> d.apply(control, t)).collect(Collectors.toList());
    return ValidationResult.fromResults(results);
  }

  public void addValidator(Validator<T> delegate) {
    delegates.add(delegate);
  }

  public boolean removeValidator(Validator<T> delegate) {
    return delegates.remove(delegate);
  }

  public List<Validator<T>> getDelegates() {
    return delegates;
  }
}
