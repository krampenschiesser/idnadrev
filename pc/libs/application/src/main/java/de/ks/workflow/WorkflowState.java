package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.eventsystem.bus.EventBus;
import de.ks.reflection.ReflectionUtil;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.cdi.WorkflowScoped;
import de.ks.workflow.validation.event.ValidationResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.Field;

/**
 *
 */
@WorkflowScoped
public class WorkflowState {
  private static final Logger log = LoggerFactory.getLogger(WorkflowState.class);
  @Inject
  protected WorkflowContext context;
  @Inject
  EventBus eventBus;

  @PostConstruct
  public void initialize() {
    eventBus.register(this);
  }

  public Object getModel() {
    return context.getWorkflow().getModel();
  }

  public Class<?> getModelClass() {
    return context.getWorkflow().getModelClass();
  }

  public Class<? extends Workflow> getWorkflowClass() {
    return context.getWorkflow().getClass();
  }

  @Subscribe
  public void onValidationSucceeded(ValidationResultEvent e) {
    if (e.isSuccessful()) {
      Object model = getModel();
      Object value = e.getValue();
      Field validatedField = e.getValidatedField();
      log.debug("Setting value {} to field {} after successful validation on {}", value, validatedField, model);
      ReflectionUtil.setField(validatedField, model, value);
    }
  }
}
