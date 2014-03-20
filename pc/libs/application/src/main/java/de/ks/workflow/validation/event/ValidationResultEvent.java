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

package de.ks.workflow.validation.event;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.validation.ConstraintViolation;
import java.lang.reflect.Field;
import java.util.Set;

/**
 *
 */
public class ValidationResultEvent {
  private boolean successful;
  private Field field;
  private final Object value;
  private final Set<ConstraintViolation<Object>> violations;

  public ValidationResultEvent(boolean successful, Field field, Object value, Set<ConstraintViolation<Object>> violations) {
    this.successful = successful;
    this.field = field;
    this.value = value;
    this.violations = violations;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public Field getValidatedField() {
    return field;
  }

  public Set<ConstraintViolation<Object>> getViolations() {
    return violations;
  }

  public Object getValue() {
    return value;
  }
}
