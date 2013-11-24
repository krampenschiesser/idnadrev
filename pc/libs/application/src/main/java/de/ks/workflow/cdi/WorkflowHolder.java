package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

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
