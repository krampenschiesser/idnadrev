package de.ks.validation.contraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotEmptyValidatorForString implements ConstraintValidator<NotEmpty, String> {
  public void initialize(NotEmpty constraintAnnotation) {
    // do nothing
  }

  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || value.length() > 0;
  }
}
