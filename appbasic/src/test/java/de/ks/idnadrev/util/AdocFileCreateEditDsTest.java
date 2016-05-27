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

package de.ks.idnadrev.util;

import com.google.common.base.StandardSystemProperty;
import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.adoc.Header;
import de.ks.idnadrev.adoc.NameStripper;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.repository.Repository;
import de.ks.util.DeleteDir;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Provider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class AdocFileCreateEditDsTest {

  private Path myRepo;
  private TestDs datasource;
  private Path tmp;
  private Path dsDir;
  private NameStripper nameStripper;

  @Before
  public void setUp() {
    tmp = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value());

    dsDir = tmp.resolve(getClass().getSimpleName());
    if (Files.exists(dsDir)) {
      new DeleteDir(dsDir).delete();
    }
    myRepo = tmp.resolve("myRepo");
    if (Files.exists(myRepo)) {
      new DeleteDir(myRepo).delete();
    }

    Repository repository = Mockito.mock(Repository.class);
    Mockito.when(repository.getPath()).thenReturn(myRepo);
    Index index = Mockito.mock(Index.class);

    nameStripper = new NameStripper();
    datasource = new TestDs(() -> repository, nameStripper, index);
  }

  @Test
  public void testCreate() throws IOException {
    AdocFile adocFile = datasource.loadModel(Object::toString);
    assertNotNull(adocFile);
    assertEquals("test.adoc", adocFile.getFileName());
    assertEquals(getClass().getSimpleName(), adocFile.getPath().getParent().getFileName().toString());
    assertTrue(adocFile.getPath().startsWith(tmp));

    adocFile.setTitle("Hello World");
    adocFile.setContent("Bla blubb");
    adocFile.getHeader().setTags(new HashSet<>(Arrays.asList("tag2", "tag1")));
    datasource.saveModel(adocFile, Object::toString);

    Path targetFolder = myRepo.resolve(new NameStripper().stripName("Hello World"));
    assertTrue(Files.exists(targetFolder));
    Path adocFilePath = targetFolder.resolve(nameStripper.stripName(adocFile.getTitle()) + ".adoc");
    assertTrue(Files.exists(adocFilePath));

    List<String> lines = Files.readAllLines(adocFilePath, StandardCharsets.UTF_8);
    assertTrue(lines.contains("= Hello World"));
    assertTrue(lines.contains(":keywords: tag1, tag2"));
    assertTrue(lines.contains("Bla blubb"));
  }

  static class TestDs extends AdocFileCreateEditDs<AdocFile> {
    public TestDs(Provider<Repository> repositoryService, NameStripper nameStripper, Index index) {
      super(AdocFile.class, AdocFileCreateEditDsTest.class.getSimpleName(), "test.adoc", p -> new AdocFile(p, null, new Header(null)), repositoryService, nameStripper, index);
    }
  }
}