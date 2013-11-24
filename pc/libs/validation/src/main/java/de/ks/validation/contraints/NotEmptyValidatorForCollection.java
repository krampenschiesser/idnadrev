package de.ks.validation.contraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;

public class NotEmptyValidatorForCollection implements ConstraintValidator<NotEmpty, Collection<?>> {
  public void initialize(NotEmpty constraintAnnotation) {
    // do nothing
  }

  public boolean isValid(Collection<?> value, ConstraintValidatorContext context) {
    return value == null || !value.isEmpty();
  }
}