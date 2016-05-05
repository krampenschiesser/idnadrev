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

package de.ks.idnadrev.repository;

import com.google.common.util.concurrent.MoreExecutors;
import de.ks.idnadrev.adoc.*;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.util.GenericDateTimeParser;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.junit.Assert.*;

public class ScannerTest {

  private Repository repo;
  private Scanner scanner;
  private Index index;
  private Path filePath;

  @Before
  public void setUp() throws Exception {
    repo = Mockito.mock(Repository.class);
    Mockito.when(repo.getDateFormatter()).thenReturn(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    Mockito.when(repo.getTimeFormatter()).thenReturn(DateTimeFormatter.ofPattern("HH:mm:ss"));

    URL resource = AdocFileParserTest.class.getResource("basic.adoc");
    filePath = Paths.get(resource.toURI());
    Path repoPath = filePath.getParent();
    Mockito.when(repo.getPath()).thenReturn(repoPath);

    index = Mockito.mock(Index.class);
    AdocFileParser adocFileParser = new AdocFileParser(new HeaderParser(new GenericDateTimeParser()));
    scanner = new Scanner(adocFileParser, index, MoreExecutors.newDirectExecutorService());
  }

  @Test
  public void scanBasicRepo() throws Exception {
    scanner.scan(repo, null);

    ArgumentCaptor<AdocFile> argumentCaptor = ArgumentCaptor.forClass(AdocFile.class);
    Mockito.verify(repo).addAdocFile(argumentCaptor.capture());

    AdocFile value = argumentCaptor.getValue();
    assertNotNull(value);
    Set<CompanionFile> files = value.getFiles();
    assertThat(files, Matchers.hasSize(2));

    CompanionFile csvFile = files.stream().filter(f -> f.getName().equals("data.csv")).findAny().get();
    assertEquals(FileType.CSV, csvFile.getFileType());

    CompanionFile imageFile = files.stream().filter(f -> f.getName().equals("test.jpg")).findAny().get();
    assertEquals(FileType.IMAGE, imageFile.getFileType());

    assertEquals(filePath, value.getPath());
    assertEquals("Document Title", value.getHeader().getTitle());

    Mockito.verify(index).add(Mockito.any(AdocFile.class));
  }
}