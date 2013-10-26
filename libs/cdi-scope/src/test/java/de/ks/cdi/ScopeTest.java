package de.ks.cdi;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.cdi.scope.StackSessionScope;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.CDI;

import static org.junit.Assert.assertTrue;

public class ScopeTest {
  private CdiContainer cdiContainer;

  @Before
  public void setUp() throws Exception {
    cdiContainer = CdiContainerLoader.getCdiContainer();
    cdiContainer.boot(null);
    cdiContainer.boot();
  }

  @After
  public void tearDown() throws Exception {
    cdiContainer.shutdown();
  }

  @Test
  public void testStackSessionScopeActive() throws Exception {
    Context context = CDI.current().getBeanManager().getContext(StackSessionScope.class);
    assertTrue(context.isActive());
  }
}
