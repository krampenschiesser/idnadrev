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

package de.ks.workflow.validation;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.bus.EventBus;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.validation.violation.IllegalArgumentViolation;
import de.ks.workflow.WorkflowState;
import de.ks.workflow.validation.event.ValidationRequiredEvent;
import de.ks.workflow.validation.event.ValidationResultEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class WorkflowModelValidator {
  @Inject
  WorkflowState workflowState;
  @Inject
  EventBus eventBus;
  @Inject
  Validator validator;

  @PostConstruct
  public void register() {
    eventBus.register(this);
  }

  @Subscribe
  @Threading(HandlingThread.Async)
  public void validate(ValidationRequiredEvent event) {
    Field field = event.getField();
    Object value = event.getValue();

    Object model = workflowState.getModel();
    @SuppressWarnings("unchecked") Class<Object> clazz = (Class<Object>) model.getClass();

    ValidationResultEvent result;
    try {
      Set<ConstraintViolation<Object>> violations = validator.validateValue(clazz, field.getName(), value);

      result = new ValidationResultEvent(violations.isEmpty(), field, value, violations);
    } catch (IllegalArgumentException e) {
      Set<ConstraintViolation<Object>> violations = new HashSet<>();
      Path path = null;
      violations.add(new IllegalArgumentViolation<>(model, field, value));
      result = new ValidationResultEvent(violations.isEmpty(), field, value, violations);
    }
    eventBus.post(result);
  }
}
