package de.ks.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class SmokeTest {
  private static final Logger log = LoggerFactory.getLogger(SmokeTest.class);

  @Test
  public void testEmergeAndFade() throws Exception {
    Smoke smoke = Smoke.instance;
    String out = smoke.emerge("test");
    String result = smoke.fadeAway(out);
    assertEquals("test", result);
  }

  @Test
  public void testFadeOnly() throws Exception {
    assertEquals("test", Smoke.instance.fadeAway("bkoq81knIShcH/7Zht1UUg=="));
  }
}