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

import javafx.util.StringConverter;

import java.util.function.Function;

public class NamedConverter<E extends Named> extends StringConverter<E> {
  Function<String, E> backFunction;

  public NamedConverter(Function<String, E> backFunction) {
    this.backFunction = backFunction;
  }

  @Override
  public String toString(E object) {
    return object.getName();
  }

  @Override
  public E fromString(String string) {
    return backFunction.apply(string);
  }
}
