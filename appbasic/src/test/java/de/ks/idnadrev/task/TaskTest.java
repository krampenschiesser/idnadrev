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
package de.ks.idnadrev.task;

import com.google.common.base.StandardSystemProperty;
import de.ks.idnadrev.adoc.AdocFileParser;
import de.ks.idnadrev.adoc.CompanionFile;
import de.ks.idnadrev.adoc.Header;
import de.ks.idnadrev.adoc.HeaderParser;
import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.util.GenericDateTimeParser;
import de.ks.util.DeleteDir;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class TaskTest {

  private Path tmp;
  private Path testDir;
  private Path myRepo;
  private Repository repository;
  private GenericDateTimeParser dateTimeParser;

  @Before
  public void setUp() throws Exception {
    tmp = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value());

    testDir = tmp.resolve(getClass().getSimpleName());
    if (Files.exists(testDir)) {
      new DeleteDir(testDir).delete();
    }
    myRepo = tmp.resolve("myRepo");
    if (Files.exists(myRepo)) {
      new DeleteDir(myRepo).delete();
    }

    dateTimeParser = new GenericDateTimeParser();
    repository = Mockito.mock(Repository.class);
    Mockito.when(repository.getPath()).thenReturn(myRepo);
    Mockito.when(repository.getDateFormatter()).thenReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    Mockito.when(repository.getTimeFormatter()).thenReturn(DateTimeFormatter.ofPattern("HH:mm:ss"));
  }

  @Test
  public void testPersistAndReadTask() throws Exception {
    LocalDateTime date = LocalDateTime.of(2016, 05, 14, 8, 45, 31);

    Path path = testDir.resolve("test.adoc");
    Task task = new Task(path, repository, new Header(repository));
    task.setTitle("title");
    task.setContent("content line 1\nline 2");
    task.setState(TaskState.ASAP);
    task.setContext("context");
    task.setEstimatedTimeInMinutes(10);
    task.getHeader().setTags(new HashSet<>(Arrays.asList("tag1", "tag2", "tag3")));
    task.getHeader().setRevDate(date, dateTimeParser);

    String content = task.writeBack();
    assertThat(content, Matchers.containsString("= title"));
    assertThat(content, Matchers.containsString(":keywords: tag1, tag2, tag3"));
    assertThat(content, Matchers.containsString(":context: context"));
    assertThat(content, Matchers.containsString(":kstype: task"));
    assertThat(content, Matchers.containsString(":state: ASAP"));
    assertThat(content, Matchers.containsString("content line 1\nline 2"));
    assertThat(content, Matchers.containsString(":estimatedtime: 10"));
    assertThat(content, Matchers.containsString(":revdate: 14.05.2016 08:45:31"));

    AdocFileParser parser = new AdocFileParser(new HeaderParser(dateTimeParser));
    Task parsed = (Task) parser.parse(path, repository, new HashSet<CompanionFile>(), Stream.of(content.split("\n")).collect(Collectors.toList()));

    assertEquals("title", parsed.getTitle());
    assertEquals("content line 1\nline 2", parsed.getContent());
    assertEquals(TaskState.ASAP, parsed.getState());
    assertEquals("context", parsed.getContext());
    assertEquals(new LinkedHashSet<>(Arrays.asList("tag1", "tag2", "tag3")), parsed.getHeader().getTags());
    assertEquals(date, parsed.getHeader().getRevDate());
    assertEquals(10, parsed.getEstimatedTimeInMinutes());
    assertEquals(10, parsed.getEstimatedTime().toMinutes());
  }

  @Test
  public void testEmptyLines() throws Exception {
    Path path = testDir.resolve("test.adoc");
    Task task = new Task(path, repository, new Header(repository));
    task.setTitle("title");
    task.setContent("bla\n\n\nblubb\n\nll");

    String content = task.writeBack();
    assertThat(content, Matchers.containsString("bla\n\n\nblubb\n\nll"));
  }
}
