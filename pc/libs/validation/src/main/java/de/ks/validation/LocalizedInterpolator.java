/*
 * Copyright [2014] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
