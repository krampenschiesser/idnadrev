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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TextParserText {
  private static final Logger log = LoggerFactory.getLogger(TextParserText.class);
  private TextParser textParser;
  private String asciiDocSimple;

  @Before
  public void setUp() throws Exception {
    asciiDocSimple = "= Simple test document =\n" +
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
            "    Attributes attributes = AttributesBuilder.attributes().linkCss(false).unsetStyleSheet().get();\n" +
            "    Options options = OptionsBuilder.options().headerFooter(true).attributes(attributes).get();//.docType(\"HTML\")\n" +
            "    String render = asciidoctor.render(input, options);\n" +
            "    return render;\n" +
            "  }" +
            "\n" +
            "----" +
            "\n";
    textParser = new TextParser();
  }

  @Test
  public void testTextParsing() throws Exception {
    String html = textParser.parse(asciiDocSimple);
    assertNotNull(html);
    log.info(html);
    assertThat(html, containsString("<html"));
    assertThat(html, not(containsString("<link rel=\"stylesheet\" href=\"./asciidoctor.css\">")));
  }
}
