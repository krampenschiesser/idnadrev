/*
 * Copyright [2016] [Christian Loehnert]
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

package de.ks.texteditor.markup.adoc;

import de.ks.texteditor.LineParser;
import de.ks.texteditor.markup.Line;
import de.ks.texteditor.markup.MarkupStyleRange;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AdocMarkupTest {

  private AdocMarkup markup;

  @Before
  public void setUp() throws Exception {
    markup = new AdocMarkup();
  }

  @Test
  public void testHeaders() throws Exception {
    List<Line> lines = new LineParser("\n").getLines("= header1\n== header2\n### header2\n## header2");
    List<MarkupStyleRange> styleRanges = markup.getStyleRanges(lines);
    assertEquals(4, styleRanges.size());
    MarkupStyleRange range = styleRanges.get(0);
    assertEquals(NewStyleHeader.ADOC_HEADER + 1, range.getStyleClass());
    assertEquals(0, range.getFromPos());
    assertEquals(9, range.getToPos());

    range = styleRanges.get(1);
    assertEquals(NewStyleHeader.ADOC_HEADER + 2, range.getStyleClass());
    assertEquals(10, range.getFromPos());
    assertEquals(20, range.getToPos());

    range = styleRanges.get(3);
    assertEquals(NewStyleHeader.ADOC_HEADER + 2, range.getStyleClass());
    assertEquals(33, range.getFromPos());
    assertEquals(43, range.getToPos());
  }

  @Test
  public void testListing() throws Exception {
    List<Line> lines = new LineParser("\n").getLines(". bla\n* blub\n** blubb\n- ah\n.. urks\n*bold*");
    List<MarkupStyleRange> styleRanges = markup.getStyleRanges(lines);
    assertEquals(5, styleRanges.size());
    assertThat(styleRanges.get(4).getStyleClass(), Matchers.not(Matchers.startsWith(ListingStyle.ADOC_LISTING)));
  }

  @Test
  public void testImage() throws Exception {
    List<Line> lines = new LineParser("\n").getLines("image::img.jpg[]");
    List<MarkupStyleRange> styleRanges = markup.getStyleRanges(lines);
    assertEquals(1, styleRanges.size());
  }

  @Test
  public void testAdocBlock() throws Exception {
    List<Line> lines = new LineParser("\n").getLines("----------------\nbla\n blubb\n-----\n----------------\n\nhuhu");

    List<MarkupStyleRange> styleRanges = markup.getStyleRanges(lines);
    assertEquals(1, styleRanges.size());
    assertEquals(0, styleRanges.get(0).getFromPos());
    assertEquals(50, styleRanges.get(0).getToPos());
  }

  @Test
  public void test2Blocks() throws Exception {
    List<Line> lines = new LineParser("\n").getLines("-----\nbla\n-----\n\n----blubb\n----");

    List<MarkupStyleRange> styleRanges = markup.getStyleRanges(lines);
    assertEquals(2, styleRanges.size());
  }

  @Test
  public void testInlineBold() throws Exception {
    List<Line> lines = new LineParser("\n").getLines("hello *world*!\n*bla* other");
    List<MarkupStyleRange> styleRanges = markup.getStyleRanges(lines);
    assertEquals(2, styleRanges.size());

    assertEquals(6, styleRanges.get(0).getFromPos());
    assertEquals(12, styleRanges.get(0).getToPos());

    assertEquals(15, styleRanges.get(1).getFromPos());
    assertEquals(19, styleRanges.get(1).getToPos());
  }

  @Test
  public void testSmallDoc() throws Exception {
    URL resource = getClass().getResource("smalldoc.adoc");
    String content = Files.readAllLines(Paths.get(resource.toURI())).stream().collect(Collectors.joining("\n"));
    List<Line> lines = new LineParser("\n").getLines(content);
    List<MarkupStyleRange> styleRanges = markup.getStyleRanges(lines);

    assertEquals(12, styleRanges.size());
  }
}