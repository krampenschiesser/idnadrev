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

package de.ks.workflow.cdi;


import de.ks.workflow.Workflow;
import de.ks.workflow.validation.WorkflowModelValidator;

import javax.enterprise.inject.spi.CDI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkflowHolder {
  private static final AtomicInteger sequence = new AtomicInteger(0);
  protected final String id = String.format("%05d", sequence.incrementAndGet());
  protected final Class<? extends Workflow> workflowClass;
  protected final Map<Class<?>, StoredBean> objectStore = new ConcurrentHashMap<>();
  protected final AtomicInteger count = new AtomicInteger(0);
  protected final WorkflowModelValidator validator;
  protected Workflow workflow;

  public WorkflowHolder(Class<? extends Workflow> workflow) {
    this.workflowClass = workflow;
    count.incrementAndGet();
    validator = CDI.current().select(WorkflowModelValidator.class).get();
  }

  public StoredBean getStoredBean(Class<?> key) {
    return objectStore.get(key);
  }

  public void put(Class<?> key, StoredBean storedBean) {
    objectStore.putIfAbsent(key, storedBean);
  }

  public Map<Class<?>, StoredBean> getObjectStore() {
    return objectStore;
  }

  public AtomicInteger getCount() {
    return count;
  }

  public Class<? extends Workflow> getWorkflowClass() {
    return workflowClass;
  }

  public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    this.workflow = workflow;
  }

  public String getId() {
    return id;
  }
}
