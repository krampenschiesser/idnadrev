package de.ks.validation.contraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;

public class NotEmptyValidatorForMap implements ConstraintValidator<NotEmpty, Map<?, ?>> {
  public void initialize(NotEmpty constraintAnnotation) {
    // do nothing
  }

  public boolean isValid(Map<?, ?> value, ConstraintValidatorContext context) {
    return value == null || !value.isEmpty();
  }
}
