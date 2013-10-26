package de.ks.scheduler;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Checks an annotated {@link CronRange} for validity.
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = CronRangeValidator.class)
@Documented
public @interface Cron {

  String message() default "{de.ks.cron.wrongpattern}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}