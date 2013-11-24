package de.ks.validation.contraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Documented
@Constraint(                                                                                  //
        validatedBy = {NotEmptyValidatorForCollection.class, NotEmptyValidatorForMap.class,   //
                NotEmptyValidatorForString.class, NotEmptyValidator.class})                   //
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotEmpty {
  Class<?>[] groups() default {};

  String message() default "{org.apache.bval.constraints.NotEmpty.message}";

  Class<? extends Payload>[] payload() default {};
}
