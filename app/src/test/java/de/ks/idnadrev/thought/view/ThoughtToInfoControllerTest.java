package de.ks.idnadrev.thought.view;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ThoughtToInfoControllerTest {
  @Test
  public void testRegex() throws Exception {
    ThoughtToInfoController controller = new ThoughtToInfoController();

    assertTrue(controller.containsHyperLinkParallel("hallo welt in www.krampenschiesser.de"));
    assertTrue(controller.containsHyperLinkParallel("www.krampenschiesser.de"));
    assertTrue(controller.containsHyperLinkParallel("http://krampenschiesser.de/blurb"));
  }
}