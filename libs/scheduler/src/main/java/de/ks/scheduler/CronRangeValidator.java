package de.ks.scheduler;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class CronRangeValidator implements ConstraintValidator<Cron, CronRange> {

  @Override
  public void initialize(Cron constraintAnnotation) {
    //
  }

  @Override
  public boolean isValid(CronRange value, ConstraintValidatorContext context) {
    List<Integer> allvalues = value.getAny();
    try {

      if (value.isAny()) {
        return true;
      }
      if (value.isList()) {
        return allvalues.containsAll(value.getList());
      }
      if (value.isRange()) {
        return allvalues.containsAll(value.getRange());
      }
      if (value.isRate()) {
        return allvalues.containsAll(value.getRate());
      }
      if (value.isSimple()) {
        return allvalues.contains(value.getSimple());
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }
}
