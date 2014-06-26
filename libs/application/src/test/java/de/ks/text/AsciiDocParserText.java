/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks.text;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class AsciiDocParserText {
  private static final Logger log = LoggerFactory.getLogger(AsciiDocParserText.class);

  private AsciiDocParser asciiDocParser;
  private String asciiDocSimple;
  private String plainText;

  @Before
  public void setUp() throws Exception {
    asciiDocSimple = "= Simple test document =\n" +
            ":linkcss:\n" +
            ":Revision: 42\n" +
            ":Author:    Hiker trash\n" +
            "\n" +
            "\n" +
            "== About ==\n" +
            "\n" +
            "Hiker trash rules.\n" +
            "Keep on hiking.\n" +
            "\n" +
            "== Code ==\n" +
            "[source,java]\n" +
            "----\n" +
            "  public String parse(String input) {\n" +
            "    Attributes attributes = AttributesBuilder.attributes()" +
            "       .linkCss(false).unsetStyleSheet().get();\n" +
            "    Options options = OptionsBuilder.options()" +
            "       .headerFooter(true).attributes(attributes).get();//.docType(\"HTML\")\n" +
            "    String render = asciidoctor.render(input, options);\n" +
            "    return render;\n" +
            "  }" +
            "\n" +
            "----" +
            "\n";
    plainText = "Hello\n" + "World.-,'*\n" + "\t==HAAAL";
    asciiDocParser = new AsciiDocParser();
  }

  @Test
  public void testTextParsing() throws Exception {
    String html = asciiDocParser.parse(asciiDocSimple);
    assertNotNull(html);
    log.info("\n" + html + "\n");
    assertThat(html, containsString("<html"));
    assertThat(html, not(containsString("<link rel=\"stylesheet\" href=\"./asciidoctor.css\">")));
    assertThat(html, not(containsString("<div id=\"footer-text\">")));
    assertThat(html, containsString(AsciiDocParser.mathJax));
  }

  @Ignore
  @Test
  public void testParsingTime() throws Exception {
    Profiler t1 = new Profiler("t1");
    t1.setLogger(log);

    t1.start("initial");
    asciiDocParser.parse(asciiDocSimple);
    t1.stop();
    t1.log();

    Profiler average = new Profiler("average");
    int runs = 100;
    for (int i = 0; i < runs; i++) {
      asciiDocParser.parse(asciiDocSimple);
    }
    average.stop();
    log.info("average execution time for {} runs is {}ms", runs, TimeUnit.NANOSECONDS.toMillis(average.elapsedTime()) / runs);
  }

  @Test
  public void testPlainTextParsing() throws Exception {
    String html = asciiDocParser.parse(plainText);
    assertNotNull(html);
  }

  @Test
  public void testMultiThreadParsing() throws Exception {
    ExecutorService service = Executors.newWorkStealingPool();
    String expectedResult = asciiDocParser.parse(asciiDocSimple);
    CompletableFuture<Void> all = null;
    try {
      for (int i = 0; i < Runtime.getRuntime().availableProcessors() * 100; i++) {
        CompletableFuture<Void> result = CompletableFuture.supplyAsync(() -> asciiDocParser.parse(asciiDocSimple), service)//
                .thenAccept((input) -> assertEquals(expectedResult, input));

        if (all == null) {
          all = result;
        } else {
          all = CompletableFuture.allOf(all, result);
        }
      }

      all.join();
    } finally {
      service.shutdownNow();
      service.awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
