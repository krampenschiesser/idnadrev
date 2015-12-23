/*
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.idnadrev.cost.pattern;

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.cost.entity.BookingPattern;
import de.ks.idnadrev.cost.module.CostModule;
import de.ks.idnadrev.cost.pattern.view.BookingPatternParser;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class BookingPatternParserTest {
  private static final Logger log = LoggerFactory.getLogger(BookingPatternParserTest.class);

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new CostModule(), new IntegrationTestModule());
  @Inject
  PersistentWork persistentWork;

  @Inject
  BookingPatternParser parser;
//  @Inject
//  Cleanup cleanup;

  @Before
  public void setUp() throws Exception {
//    cleanup.cleanup();

    persistentWork.persist(new BookingPattern("test").setCategory("category1").setRegex("bla").setSimpleContains(true));
  }

  @Test
  public void testBookingParser() throws Exception {
    assertNull(parser.parseLine("blubb"));
    assertNotNull(parser.parseLine("bla"));
    assertEquals("category1", parser.parseLine("bla"));

    log.info("addding 2nd pattern");
    persistentWork.persist(new BookingPattern("test2").setCategory("hello").setRegex("blubb").setSimpleContains(true));
    assertNotNull(parser.parseLine("blubb"));
    assertEquals("hello", parser.parseLine("blubb"));

    persistentWork.removeAllOf(BookingPattern.class);

    persistentWork.persist(new BookingPattern("test").setCategory("category1").setRegex("bla").setSimpleContains(true));
    log.info("removing 2nd pattern");
    assertNull(parser.parseLine("blubb"));
    assertNotNull(parser.parseLine("bla"));

    log.info("Updating existing pattern");
    persistentWork.run(session -> {
      persistentWork.forName(BookingPattern.class, "test").setCategory("sauerland");
    });
    assertEquals("sauerland", parser.parseLine("bla"));

    log.info("nothing changed");
    assertEquals("sauerland", parser.parseLine("bla"));
  }
}
