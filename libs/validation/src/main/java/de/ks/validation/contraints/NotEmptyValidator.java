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

package de.ks.validation.contraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> {
  public void initialize(NotEmpty constraintAnnotation) {
    // do nothing
  }

  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    if (value.getClass().isArray()) {
      return Array.getLength(value) > 0;
    } else {
      try {
        Method isEmptyMethod = value.getClass().getMethod("isEmpty");
        if (isEmptyMethod != null) {
          return !((Boolean) isEmptyMethod.invoke(value)).booleanValue();
        }
      } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException iae) {
        // do nothing
      }
      return value.toString().length() > 0;
    }
  }
}