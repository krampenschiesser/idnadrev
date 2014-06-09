/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

import de.ks.i18n.Localized;
import javafx.scene.control.Control;
import org.controlsfx.validation.Severity;

public class ValidationMessage implements org.controlsfx.validation.ValidationMessage {

  private final String messageTemplate;
  private final Severity severity;
  private final Control target;
  private final Object[] parameters;

  public ValidationMessage(String messageTemplate, Control target, Object... parameters) {
    this(messageTemplate, Severity.ERROR, target, parameters);
  }

  public ValidationMessage(String messageTemplate, Severity severity, Control target, Object... parameters) {
    assert messageTemplate != null;
    this.messageTemplate = messageTemplate.replaceAll("\\{", "").replaceAll("\\}", "");
    this.severity = severity;
    this.target = target;
    this.parameters = parameters == null ? new Object[0] : parameters;
  }

  @Override
  public String getText() {
    if (messageTemplate != null) {
      return Localized.get(messageTemplate, parameters);
    } else {
      return null;
    }
  }

  @Override
  public Severity getSeverity() {
    return severity;
  }

  @Override
  public Control getTarget() {
    return target;
  }
}
