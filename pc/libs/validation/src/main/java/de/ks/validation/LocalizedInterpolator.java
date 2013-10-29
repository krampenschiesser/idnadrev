package de.ks.validation;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.validation.MessageInterpolator;
import javax.validation.ValidatorFactory;
import java.util.Locale;

/**
 *
 */
class LocalizedInterpolator implements MessageInterpolator {
  private final MessageInterpolator delegate;

  protected LocalizedInterpolator() {
    ValidatorFactory validatorFactory = javax.validation.Validation.buildDefaultValidatorFactory();
    delegate = validatorFactory.getMessageInterpolator();
  }

  @Override
  public String interpolate(String messageTemplate, Context context) {

//return     Localized.get(messageTemplate,context.getValidatedValue());
    return delegate.interpolate(messageTemplate, context);
  }

  @Override
  public String interpolate(String messageTemplate, Context context, Locale locale) {
    return delegate.interpolate(messageTemplate, context, locale);
  }
}
