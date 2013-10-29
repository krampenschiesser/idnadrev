package de.ks.validation;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 */
public class Validation {
  public static Validator getValidator() {
    ValidatorFactory validatorFactory = javax.validation.Validation.buildDefaultValidatorFactory();
    return validatorFactory.usingContext().messageInterpolator(new LocalizedInterpolator()).getValidator();
  }
}
