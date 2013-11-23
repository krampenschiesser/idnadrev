package de.ks.workflow.specific;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.workflow.cdi.TestWorkflow1;
import de.ks.workflow.cdi.WorkflowSpecific;

@WorkflowSpecific(TestWorkflow1.class)
public class TestSpecialBean1 extends TestBean {
  @Override
  public String get() {
    return TestSpecialBean1.class.getName();
  }
}
