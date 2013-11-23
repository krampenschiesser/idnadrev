package de.ks.workflow.specific;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.workflow.cdi.WorkflowSpecificLiteral;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static org.junit.Assert.assertFalse;

@RunWith(JFXCDIRunner.class)
public class InjectionTest {
  public static final String WORKFLOW_HELLO = "hello";
  public static final String WORKFLOW_SAUERLAND = "sauerland";

  @Inject
  @Any
  Instance<Object> provider;

  @Test
  public void testInheritanceSpecialization() {
    assertFalse(provider.select(TestBean.class, new DefaultLiteral()).isUnsatisfied());

    assertFalse(provider.select(TestBean.class, new WorkflowSpecificLiteral(WORKFLOW_HELLO)).isUnsatisfied());
    assertFalse(provider.select(TestBean.class, new WorkflowSpecificLiteral(WORKFLOW_SAUERLAND)).isUnsatisfied());
  }
}
