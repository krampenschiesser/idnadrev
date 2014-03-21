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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class WorkflowContext implements Context {
  private static final Logger log = LoggerFactory.getLogger(WorkflowContext.class);
  protected final ConcurrentHashMap<Class<? extends Workflow>, WorkflowHolder> workflows = new ConcurrentHashMap<>();
  protected final ThreadLocal<LinkedList<Class<? extends Workflow>>> workflowStack = ThreadLocal.withInitial(LinkedList::new);

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

  private final BeanManager beanManager;

  public static void start(Class<? extends Workflow> workflowClass) {
    WorkflowContext context = (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
    context.startWorkflow(workflowClass);
  }

  public static void stop(Class<? extends Workflow> workflowClass) {
    WorkflowContext context = (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
    context.stopWorkflow(workflowClass);
  }

  public static void stopAll() {
    WorkflowContext context = (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
    context.cleanupAllWorkflows();
  }

  public WorkflowContext(BeanManager mgr) {
    beanManager = mgr;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return WorkflowScoped.class;
  }

  @Override
  public <T> T get(Contextual<T> contextual) {
    if (contextual instanceof Bean) {
      Bean bean = (Bean) contextual;

      Pair<Class<?>, Class<?>> key = getKey(bean);
      lock.readLock().lock();
      try {
        StoredBean storedBean = workflows.get(key.getLeft()).getStoredBean(key.getRight());
        if (storedBean != null) {
          return storedBean.getInstance();
        }
      } finally {
        lock.readLock().unlock();
      }
    }
    return null;
  }

  @Override
  public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
    if (contextual instanceof Bean) {
      Bean bean = (Bean) contextual;
      Pair<Class<?>, Class<?>> key = getKey(bean);

      lock.writeLock().lock();
      try {
        Object o = bean.create(creationalContext);
        StoredBean storedBean = new StoredBean(bean, creationalContext, o);
        workflows.get(key.getLeft()).put(key.getRight(), storedBean);
        return (T) o;
      } finally {
        lock.writeLock().unlock();
      }
    }
    return null;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  protected Pair<Class<?>, Class<?>> getKey(Bean<?> bean) {
    Class<?> beanClass = bean.getBeanClass();
    Annotation annotation = beanClass.getAnnotation(WorkflowScoped.class);

    if (annotation instanceof WorkflowScoped) {
      if (workflowStack.get().isEmpty()) {
        throw new IllegalStateException("No workflow currently active!");
      }
      Class<?> currentWorkflow = this.workflowStack.get().getLast();
      Pair<Class<?>, Class<?>> pair = (Pair<Class<?>, Class<?>>) Pair.of(currentWorkflow, beanClass);
      return pair;
    } else {
      throw new IllegalStateException("Unable to retrieve " + WorkflowScoped.class.getName());
    }
  }

  protected void cleanupSingleWorkflow(Class<? extends Workflow> workflowClass) {
    lock.writeLock().lock();
    try {
      WorkflowHolder workflow = workflows.remove(workflowClass);
      log.debug("Cleanup workflow {}->{}", workflow.getId(), workflowClass.getSimpleName());
      Set<Map.Entry<Class<?>, StoredBean>> entries = workflow.objectStore.entrySet();

      for (Iterator<Map.Entry<Class<?>, StoredBean>> iterator = entries.iterator(); iterator.hasNext(); ) {
        Map.Entry<Class<?>, StoredBean> next = (Map.Entry<Class<?>, StoredBean>) iterator.next();
        next.getValue().destroy();
        iterator.remove();
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public WorkflowHolder startWorkflow(Class<? extends Workflow> workflowClass) {
    lock.writeLock().lock();
    try {
      LinkedList<Class<? extends Workflow>> workflows = workflowStack.get();
      workflows.add(workflowClass);
      WorkflowHolder workflow = new WorkflowHolder(workflowClass);
      this.workflows.put(workflowClass, workflow);

      Workflow workflowInstance = CDI.current().select(workflowClass).get();
      workflow.setWorkflow(workflowInstance);
      workflowInstance.getModel();//initialize workflow
      log.debug("Started workflow {}->{}", workflow.getId(), workflowClass.getSimpleName());
      return workflow;
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void registerPlannedPropagation(Class<? extends Workflow> workflowClass) {
    lock.writeLock().lock();
    try {
      WorkflowHolder workflowHolder = workflows.get(workflowClass);
      workflowHolder.getCount().incrementAndGet();
      log.debug("Registered planned propagation for workflow {}->{}", workflowHolder.getId(), workflowClass.getSimpleName());
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void propagateWorkflow(Class<? extends Workflow> workflowClass) {
    lock.writeLock().lock();
    try {
      workflowStack.get().add(workflowClass);
      WorkflowHolder workflowHolder = workflows.get(workflowClass);
      log.debug("Propagated workflow {}->{}", workflowHolder.getId(), workflowClass.getSimpleName());
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void stopWorkflow(Class<? extends Workflow> workflowClass) {
    lock.writeLock().lock();
    try {
      workflowStack.get().remove(workflowClass);
      WorkflowHolder workflowHolder = workflows.get(workflowClass);
      int count = workflowHolder.getCount().decrementAndGet();
      if (count == 0) {
        cleanupSingleWorkflow(workflowClass);
      }
      log.debug("Stopped workflow {}->{}", workflowHolder.getId(), workflowClass.getSimpleName());
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void cleanupAllWorkflows() {
    log.debug("Cleanup all activities.");
    for (Class<? extends Workflow> workflow : workflowStack.get()) {
      WorkflowHolder workflowHolder = workflows.get(workflow);
      if (workflowHolder.getCount().get() > 1) {
        log.warn("There are still {} other threads holding a reference to this workflow, cleanup not allowed", workflowHolder.getCount().get() - 1);
      }
      while (workflowHolder.getCount().get() > 1) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          log.error("Interrupted", e);
        }
      }
      cleanupSingleWorkflow(workflow);
    }
    lock.writeLock().lock();
    try {
      this.workflows.clear();
      workflowStack.get().clear();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public Workflow getWorkflow() {
    if (this.workflowStack.get().isEmpty()) {
      throw new RuntimeException("No workflow active in current thread!");
    }
    Class<? extends Workflow> currentWorkflow = this.workflowStack.get().getLast();
    WorkflowHolder holder = workflows.get(currentWorkflow);
    return holder.getWorkflow();
  }

  public WorkflowHolder getHolder() {
    if (this.workflowStack.get().isEmpty()) {
      throw new RuntimeException("No workflow active in current thread!");
    }
    Class<? extends Workflow> currentWorkflow = this.workflowStack.get().getLast();
    WorkflowHolder holder = workflows.get(currentWorkflow);
    return holder;
  }
}
