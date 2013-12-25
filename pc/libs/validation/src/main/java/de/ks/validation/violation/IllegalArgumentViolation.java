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
    this.messageTemplate = "/validation/error/illegalValue";
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
