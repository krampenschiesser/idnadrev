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

package de.ks.javafx.converter;

import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

import java.text.ParseException;

/**
 * If an exception occurs during string parsing the last value and not the default value is used
 */
public class LastValueConverter<T, C extends StringConverter<T>> extends StringConverter<T> {
  private final C delegate;
  private final ObservableValue<T> valueProperty;

  /**
   * @param delegate      used for the default values
   * @param valueProperty used to return the last value in case of an error
   */
  public LastValueConverter(C delegate, ObservableValue<T> valueProperty) {
    this.delegate = delegate;
    this.valueProperty = valueProperty;
  }

  @Override
  public String toString(T t) {
    return delegate.toString(t);
  }

  @Override
  public T fromString(String s) {
    try {
      return delegate.fromString(s);
    } catch (NumberFormatException e) {
      return valueProperty.getValue();
    } catch (RuntimeException e) {
      if (e.getCause() instanceof ParseException) {
        return valueProperty.getValue();
      }
      throw e;
    }
  }
}
