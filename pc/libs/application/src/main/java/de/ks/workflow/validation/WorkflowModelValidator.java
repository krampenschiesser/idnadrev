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
import de.ks.workflow.WorkflowState;
import de.ks.workflow.validation.event.ValidationRequiredEvent;
import de.ks.workflow.validation.event.ValidationResultEvent;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.Field;
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

    Object model = workflowState.getModel();
    Class<Object> clazz = (Class<Object>) model.getClass();

    Set<ConstraintViolation<Object>> violations = validator.validateValue(clazz, field.getName(), event.getValue());

    ValidationResultEvent result = new ValidationResultEvent(violations.isEmpty(), field);
    eventBus.post(result);
  }
}
