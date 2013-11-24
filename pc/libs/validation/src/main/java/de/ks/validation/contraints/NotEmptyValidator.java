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
    if (value == null) return true;
    if (value.getClass().isArray()) {
      return Array.getLength(value) > 0;
    } else {
      try {
        Method isEmptyMethod = value.getClass().getMethod("isEmpty");
        if (isEmptyMethod != null) {
          return !((Boolean) isEmptyMethod.invoke(value)).booleanValue();
        }
      } catch (IllegalAccessException iae) {
        // do nothing
      } catch (NoSuchMethodException nsme) {
        // do nothing
      } catch (InvocationTargetException ite) {
        // do nothing
      }
      return value.toString().length() > 0;
    }
  }
}