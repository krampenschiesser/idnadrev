/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.workflow;


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
