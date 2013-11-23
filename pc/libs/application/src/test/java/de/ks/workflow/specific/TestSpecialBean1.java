package de.ks.workflow.specific;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowSpecific;

@WorkflowSpecific(InjectionTest.WORKFLOW_HELLO)
public class TestSpecialBean1 extends TestBean {
  @Override
  public String get() {
    return TestSpecialBean1.class.getName();
  }
}
