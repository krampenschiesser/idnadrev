package de.ks.workflow.specific;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.WorkflowSpecific;

@WorkflowSpecific(InjectionTest.WORKFLOW_SAUERLAND)
public class TestSpecialBean2 extends TestBean {
  @Override
  public String get() {
    return TestSpecialBean2.class.getName();
  }
}
