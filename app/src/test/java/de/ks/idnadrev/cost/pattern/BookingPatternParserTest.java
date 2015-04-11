package de.ks.idnadrev.cost.pattern;

import de.ks.LauncherRunner;
import de.ks.idnadrev.cost.pattern.view.BookingPatternParser;
import de.ks.idnadrev.entity.Cleanup;
import de.ks.idnadrev.entity.cost.BookingPattern;
import de.ks.persistence.PersistentWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class BookingPatternParserTest {
  private static final Logger log = LoggerFactory.getLogger(BookingPatternParserTest.class);
  @Inject
  BookingPatternParser parser;
  @Inject
  Cleanup cleanup;

  @Before
  public void setUp() throws Exception {
    cleanup.cleanup();

    PersistentWork.persist(new BookingPattern("test").setCategory("category1").setRegex("bla").setSimpleContains(true));
  }

  @Test
  public void testBookingParser() throws Exception {
    assertNull(parser.parseLine("blubb"));
    assertNotNull(parser.parseLine("bla"));
    assertEquals("category1", parser.parseLine("bla"));

    log.info("addding 2nd pattern");
    PersistentWork.persist(new BookingPattern("test2").setCategory("hello").setRegex("blubb").setSimpleContains(true));
    assertNotNull(parser.parseLine("blubb"));
    assertEquals("hello", parser.parseLine("blubb"));

    PersistentWork.deleteAllOf(BookingPattern.class);

    PersistentWork.persist(new BookingPattern("test").setCategory("category1").setRegex("bla").setSimpleContains(true));
    log.info("removing 2nd pattern");
    assertNull(parser.parseLine("blubb"));
    assertNotNull(parser.parseLine("bla"));

    log.info("Updating existing pattern");
    PersistentWork.wrap(() -> {
      PersistentWork.forName(BookingPattern.class, "test").setCategory("sauerland");
    });
    assertEquals("sauerland", parser.parseLine("bla"));

    log.info("nothing changed");
    assertEquals("sauerland", parser.parseLine("bla"));
  }
}
