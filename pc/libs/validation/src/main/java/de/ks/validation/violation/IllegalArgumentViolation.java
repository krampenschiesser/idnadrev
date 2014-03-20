/*
 * Copyright [2014] [Christian Loehnert]
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

package de.ks.validation.violation;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.i18n.Localized;
import de.ks.validation.FieldPath;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.reflect.Field;

/**
 *
 */
public class IllegalArgumentViolation<T> implements ConstraintViolation<T> {
  private String message;
  private String messageTemplate;
  private T rootBean;
  private Class<T> rootBeanClass;
  private Path path;
  private Object invalidvalue;

  @SuppressWarnings("unchecked")
  public IllegalArgumentViolation(T rootBean, Field field, Object invalidValue) {
    this.messageTemplate = "validation.errorillegalValue";
    this.message = Localized.get(messageTemplate, invalidValue);
    this.rootBean = rootBean;
    this.rootBeanClass = (Class<T>) field.getDeclaringClass();

    this.path = new FieldPath(field);
    this.invalidvalue = invalidValue;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getMessageTemplate() {
    return messageTemplate;
  }

  @Override
  public T getRootBean() {
    return rootBean;
  }

  @Override
  public Class<T> getRootBeanClass() {
    return rootBeanClass;
  }

  @Override
  public Object getLeafBean() {
    return null;
  }

  @Override
  public Object[] getExecutableParameters() {
    return new Object[0];
  }

  @Override
  public Object getExecutableReturnValue() {
    return null;
  }

  @Override
  public Path getPropertyPath() {
    return path;
  }

  @Override
  public Object getInvalidValue() {
    return invalidvalue;
  }

  @Override
  public ConstraintDescriptor<?> getConstraintDescriptor() {
    return null;
  }

  @Override
  public <U> U unwrap(Class<U> type) {
    throw new javax.validation.ValidationException("Not supported.");
  }
}
