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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

public class AdocFileParserTest {
  private Repository repo;
  private AdocFileParser adocFileParser;

  @Before
  public void setUp() throws Exception {
    repo = Mockito.mock(Repository.class);
    Mockito.when(repo.getDateFormatter()).thenReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    Mockito.when(repo.getTimeFormatter()).thenReturn(DateTimeFormatter.ofPattern("HH:mm:ss"));
    HeaderParser headerParser = new HeaderParser(new GenericDateTimeParser());
    adocFileParser = new AdocFileParser(headerParser);
  }

  @Test
  public void testParseAdocFile() throws Exception {
    Path path = Paths.get(getClass().getResource("basic.adoc").toURI().getPath());
    AdocFile adocFile = adocFileParser.parse(path, repo);

    assertEquals(path, adocFile.getPath());

    assertEquals(21, adocFile.getLines().size());
    assertEquals("A simple http://asciidoc.org[AsciiDoc] document.", adocFile.getLines().get(0));
    assertEquals("Document Title", adocFile.getHeader().getTitle());
  }
}