package de.ks.workflow.cdi;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */


import de.ks.workflow.Workflow;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  private static final Logger log = LogManager.getLogger(WorkflowContext.class);
  protected final ConcurrentHashMap<String, WorkflowHolder> workflows = new ConcurrentHashMap<>();
  protected final ThreadLocal<LinkedList<String>> workflowStack = ThreadLocal.withInitial(LinkedList::new);

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

  private final BeanManager beanManager;

  public static void start(String name) {
  }

  public static void start(Class<? extends Workflow> workflowClass) {
    WorkflowContext context = (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
    context.startWorkflow(workflowClass);
  }

  public static void stop(String name) {
    WorkflowContext context = (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
    context.stopWorkflow(name);
  }

  public static void stopAll() {
    WorkflowContext context = (WorkflowContext) CDI.current().getBeanManager().getContext(WorkflowScoped.class);
    context.stopAllWorkflows();
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

      Pair<String, Class<?>> key = getKey(bean);
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
      Pair<String, Class<?>> key = getKey(bean);

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

  protected Pair<String, Class<?>> getKey(Bean<?> bean) {
    Class<?> beanClass = bean.getBeanClass();
    Annotation annotation = beanClass.getAnnotation(WorkflowScoped.class);

    if (annotation instanceof WorkflowScoped) {
      if (workflowStack.get().isEmpty()) {
        throw new IllegalStateException("No workflow currently active!");
      }
      String currentWorkflowId = this.workflowStack.get().getLast();
      Pair<String, Class<?>> pair = (Pair<String, Class<?>>) Pair.of(currentWorkflowId, beanClass);
      return pair;
    } else {
      throw new IllegalStateException("Unable to retrieve " + WorkflowScoped.class.getName());
    }
  }

  protected void cleanupSingleWorkflow(String name) {
    boolean locked = lock.writeLock().tryLock();
    try {
      log.debug("Cleanup workflow {}", name);
      WorkflowHolder workflow = workflows.remove(name);
      Set<Map.Entry<Class<?>, StoredBean>> entries = workflow.objectStore.entrySet();

      for (Iterator<Map.Entry<Class<?>, StoredBean>> iterator = entries.iterator(); iterator.hasNext(); ) {
        Map.Entry<Class<?>, StoredBean> next = (Map.Entry<Class<?>, StoredBean>) iterator.next();
        next.getValue().destroy();
        iterator.remove();
      }
    } finally {
      if (locked) {
        lock.writeLock().unlock();
      }
    }
  }

  public void startWorkflow(Class<? extends Workflow>  workflowClass) {
    String name = workflowClass.getName();
    lock.writeLock().lock();
    try {
      log.debug("Starting workflow {}", name);
      LinkedList<String> workflowNames = workflowStack.get();
      workflowNames.add(name);
      WorkflowHolder workflow = new WorkflowHolder(name);
      this.workflows.put(name, workflow);
      CDI.current().select(workflowClass).get().getModel();//initialize workflow
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void propagateWorkflow(String name) {
    lock.writeLock().lock();
    try {
      log.debug("Propagating workflow {}", name);
      workflowStack.get().add(name);
      workflows.get(name).getCount().incrementAndGet();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void stopWorkflow(String name) {
    lock.writeLock().lock();
    try {
      log.debug("Stopping workflow {}", name);
      workflowStack.get().remove(name);
      int count = workflows.get(name).getCount().decrementAndGet();
      if (count == 0) {
        cleanupSingleWorkflow(name);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void stopAllWorkflows() {
    lock.writeLock().lock();
    try {
      LinkedList<String> workflows = workflowStack.get();
      for (String workflow : workflows) {
        cleanupSingleWorkflow(workflow);
      }
      this.workflows.clear();
      workflowStack.get().clear();
    } finally {
      lock.writeLock().unlock();
    }
  }
}
