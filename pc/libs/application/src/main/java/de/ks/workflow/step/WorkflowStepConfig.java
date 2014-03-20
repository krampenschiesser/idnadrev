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

package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.executor.ExecutorService;
import de.ks.workflow.WorkflowConfig;

import javax.enterprise.inject.spi.CDI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class WorkflowStepConfig {
  private final WorkflowConfig config;
  private final Class<? extends WorkflowStep> implementationClass;
  private final String incomingKey;
  private final Map<String, WorkflowStepConfig> outputs = new HashMap<>();
  private WorkflowStep step;

  public WorkflowStepConfig(WorkflowConfig config, Class<? extends WorkflowStep> step, String incomingKey) {
    this.config = config;
    this.implementationClass = step;
    this.incomingKey = incomingKey;
  }

  public WorkflowStepConfig next(Class<? extends WorkflowStep> step) {
    return next(new WorkflowStepConfig(config, step, DefaultOutput.NEXT.name()));
  }

  public WorkflowStepConfig next(WorkflowStepConfig cfg) {
    outputs.put(cfg.getIncomingKey(), cfg);
    return cfg;
  }

  public WorkflowStepConfig error(Class<? extends WorkflowStep> step) {
    return error(new WorkflowStepConfig(config, step, DefaultOutput.ERROR.name()));
  }

  public WorkflowStepConfig error(WorkflowStepConfig cfg) {
    outputs.put(cfg.getIncomingKey(), cfg);
    return cfg;
  }

  public WorkflowStepConfig restartWithNext() {
    outputs.put(DefaultOutput.NEXT.name(), config.getRoot());
    return config.getRoot();
  }

  public WorkflowStepConfig branch(String outputValue, WorkflowStepConfig step) {
    outputs.put(outputValue, step);
    return step;
  }

  public WorkflowStepConfig branch(String outputValue, Class<? extends WorkflowStep> step) {
    return branch(outputValue, new WorkflowStepConfig(config, step, outputValue));
  }


  public boolean hasOutput(String name) {
    return outputs.containsKey(name);
  }

  public WorkflowStepConfig getOutput(String name) {
    return outputs.get(name);
  }

  public Collection<WorkflowStepConfig> getOutputs() {
    return outputs.values();
  }

  public String getIncomingKey() {
    return incomingKey;
  }

  public <T extends WorkflowStep> T getStep() {
    if (step == null) {
      step = ExecutorService.instance.loadInJavaFXThread(() -> CDI.current().select(implementationClass).get());
    }
    return (T) step;
  }
}
