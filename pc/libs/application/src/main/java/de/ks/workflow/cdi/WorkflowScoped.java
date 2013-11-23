package de.ks.workflow.cdi;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.context.NormalScope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@NormalScope
public @interface WorkflowScoped {
}
