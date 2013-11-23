package de.ks.workflow.specific;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.TestWorkflow2;
import de.ks.workflow.cdi.WorkflowSpecific;

@WorkflowSpecific(TestWorkflow2.class)
public class TestSpecialBean2 extends TestBean {
  @Override
  public String get() {
    return TestSpecialBean2.class.getName();
  }
}
