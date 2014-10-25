package de.ks.option.properties;

import de.ks.LauncherRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class PropertiesOptionSourceTest {
  @Before
  public void setUp() throws Exception {
    PropertiesOptionSource.cleanup();
  }

  @Test
  public void testInt() throws Exception {
    PropertiesOptionSource source = new PropertiesOptionSource();
    source.saveOption("test.bla", 24);

    Object retval = source.readOption("test.bla");
    assertEquals(24, retval);

    source.saveOption("blub", new TestOptionContainer());
    TestOptionContainer blub = source.readOption("blub");
    assertEquals(42, blub.number);
    assertEquals("beer", blub.name);
  }
}