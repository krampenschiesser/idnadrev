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

import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.util.GenericDateTimeParser;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AdocFileParserTest {
  private Repository repo;
  private AdocFileParser adocFileParser;
  private GenericDateTimeParser dateTimeParser;

  @Before
  public void setUp() throws Exception {
    repo = Mockito.mock(Repository.class);
    Mockito.when(repo.getDateFormatter()).thenReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    Mockito.when(repo.getTimeFormatter()).thenReturn(DateTimeFormatter.ofPattern("HH:mm:ss"));
    dateTimeParser = new GenericDateTimeParser();
    HeaderParser headerParser = new HeaderParser(dateTimeParser);
    adocFileParser = new AdocFileParser(headerParser);
  }

  @Test
  public void testParseAdocFile() throws Exception {
    Path path = Paths.get(getClass().getResource("basic.adoc").toURI());
    AdocFile adocFile = adocFileParser.parse(path, repo);

    assertEquals(path, adocFile.getPath());

    assertEquals(32, adocFile.getLines().size());
    assertEquals("A simple http://asciidoc.org[AsciiDoc] document.", adocFile.getLines().get(0));
    assertEquals("Document Title", adocFile.getHeader().getTitle());
  }

  @Test
  public void testWriteBack() throws Exception {

    Path path = Paths.get(getClass().getResource("basic.adoc").toURI());
    AdocFile adocFile = adocFileParser.parse(path, repo);

    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    String expected = lines.stream().collect(Collectors.joining("\n"));

    String output = adocFile.writeBack();
    String replace = StringUtils.replace(output, "\r\n", "\n");
    assertEquals(expected, replace);
  }

  @Test
  public void testWriteBackHeader() throws Exception {
    List<String> lines = Arrays.asList(//
      "= Title",//
      ":author: scar",//
      ":revdate: 20.01.2015 20:30",//
      ":keywords: hello, sauerland", "");

    AdocFile adocFile = adocFileParser.parse(null, repo, new HashSet<CompanionFile>(), lines);

    String expected = lines.stream().collect(Collectors.joining("\n"));
    String output = adocFile.writeBack();
    String replace = StringUtils.replace(output, "\r\n", "\n");
    assertEquals(expected, replace);
  }

  @Test
  public void testWriteBackHeaderDate() throws Exception {
    List<String> lines = Arrays.asList(//
      "= Title",//
      ":author: scar",//
      ":revdate: 20.01.2015 20:30",//
      ":keywords: hello, sauerland", "");

    AdocFile adocFile = adocFileParser.parse(null, repo, new HashSet<CompanionFile>(), lines);

    lines.set(2, ":revdate: 25.02.2015 20:10:00");
    String expected = lines.stream().collect(Collectors.joining("\n"));
    adocFile.getHeader().setRevDate(LocalDateTime.of(2015, 2, 25, 20, 10), dateTimeParser);

    String output = adocFile.writeBack();
    String replace = StringUtils.replace(output, "\r\n", "\n");
    assertEquals(expected, replace);
  }

  @Test
  public void testParseTask() throws Exception {
    List<String> lines = Arrays.asList(//
      "= Title",//
      ":author: scar",//
      ":revdate: 20.01.2015 20:30",//
      ":kstype: task",//
      ":keywords: hello, sauerland", "");
    AdocFile adocFile = adocFileParser.parse(null, repo, new HashSet<CompanionFile>(), lines);

    assertThat(adocFile, Matchers.instanceOf(Task.class));
  }
}