package de.ks.workflow.cdi;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.Workflow;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines that a bean is workflow specific.
 * Use this to mark a specialization(inheriting) bean as the specific bean for a workflow.
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface WorkflowSpecific {
  Class<? extends Workflow> value();
}
