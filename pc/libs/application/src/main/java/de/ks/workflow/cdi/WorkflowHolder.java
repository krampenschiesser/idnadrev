package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.validation.WorkflowModelValidator;

import javax.enterprise.inject.spi.CDI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkflowHolder {
  private final String name;
  protected Map<Class<?>, StoredBean> objectStore = new ConcurrentHashMap<>();
  protected AtomicInteger count = new AtomicInteger(0);
  protected final WorkflowModelValidator validator;

  public WorkflowHolder(String name) {
    this.name = name;
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

  public String getName() {
    return name;
  }
}
