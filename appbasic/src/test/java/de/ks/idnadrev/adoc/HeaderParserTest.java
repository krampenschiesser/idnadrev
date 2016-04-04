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

package de.ks.idnadrev.adoc;

import de.ks.idnadrev.GenericDateTimeParser;
import de.ks.idnadrev.repository.Repository;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HeaderParserTest {

  private HeaderParser headerParser;
  private Repository repo;

  @Before
  public void setUp() throws Exception {
    repo = Mockito.mock(Repository.class);
    Mockito.when(repo.getDateFormatter()).thenReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    Mockito.when(repo.getTimeFormatter()).thenReturn(DateTimeFormatter.ofPattern("HH:mm:ss"));
    headerParser = new HeaderParser(new GenericDateTimeParser());
  }

  @Test
  public void testParseHeaderWithDate() throws Exception {
    List<String> lines = Arrays.asList(//
      "= Title\n",//
      ":author:scar\n",//
      ":revdate: 20.01.2015\n",//
      ":keywords: hello, sauerland \n");
    Header header = headerParser.parse(lines, null, repo).getHeader();

    LocalDateTime date = LocalDateTime.of(2015, 1, 20, 0, 0);
    HashSet<String> tags = new HashSet<String>() {{
      add("hello");
      add("sauerland");
    }};
    assertEquals("scar", header.getAuthor());
    assertEquals(tags, header.getTags());
    assertEquals(date, header.getRevDate());

    assertEquals("scar", header.getHeaderElement("author"));
    assertEquals("20.01.2015", header.getHeaderElement("revdate"));
  }

  @Test
  public void testParseHeaderWithDateTime() throws Exception {
    List<String> lines = Arrays.asList(//
      "= Title\n",//
      ":author: scar\n",//
      ":revdate: 20.01.2015 20:30\n",//
      ":keywords: hello, sauerland \n");
    Header header = headerParser.parse(lines, null, repo).getHeader();

    LocalDateTime date = LocalDateTime.of(2015, 1, 20, 20, 30);
    assertEquals(date, header.getRevDate());
  }

  @Test
  public void testStandardHeader() throws Exception {
    String description = "This story chronicles the inexplicable hazards and vicious beasts a +\n" +
      "team must conquer and vanquish on their journey to discovering open source's true +\n" +
      "power.";
    String adoc = "= The Dangerous & _Thrilling_ Documentation Chronicles: Based on True Events\n" +
      "Kismet Caméléon; Lazarus het_Draeke\n" +
      "v1.0, 01.01.2014: The first incarnation of {doctitle}\n" +
      ":description: This story chronicles the inexplicable hazards and vicious beasts a +\n" +
      "team must conquer and vanquish on their journey to discovering open source's true +\n" +
      "power.\n" +
      ":doctype: book\n";
    List<String> lines = new ArrayList<>(Arrays.asList(StringUtils.split(adoc, "\n")));
    lines.add("");
    Header header = headerParser.parse(lines, null, repo).getHeader();
    assertEquals("Kismet Caméléon; Lazarus het_Draeke", header.getHeaderElement(Header.AUTHOR_LINE));
    assertEquals(LocalDateTime.of(2014, 1, 1, 0, 0, 0), header.getRevDate());
    assertEquals(description, header.getHeaderElement("description"));
    assertEquals("book", header.getHeaderElement("doctype"));
  }
}