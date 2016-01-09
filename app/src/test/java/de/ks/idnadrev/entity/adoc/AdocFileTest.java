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

package de.ks.idnadrev.entity.adoc;

import com.google.common.base.StandardSystemProperty;
import de.ks.flatadocdb.Repository;
import de.ks.flatadocdb.session.SessionFactory;
import de.ks.flatadocdb.util.DeleteDir;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AdocFileTest {

  private SessionFactory sessionFactory;
  private Path repoPath;
  private Repository repository;
  protected String adocContent;

  @Before
  public void setUp() throws Exception {
    repoPath = Files.createTempDirectory("adocFileTest");
    repository = new Repository(repoPath);
    sessionFactory = new SessionFactory(repository, AdocFile.class);

    URL resource = getClass().getResource("test.adoc");
    Path path = Paths.get(resource.toURI());
    adocContent = Files.readAllLines(path, StandardCharsets.UTF_8).stream().collect(Collectors.joining(StandardSystemProperty.LINE_SEPARATOR.value()));

  }

  @After
  public void tearDown() throws Exception {
    sessionFactory.close();
    new DeleteDir(repoPath).delete();
  }

  @Test
  public void testPersist() throws Exception {
    sessionFactory.transactedSession(session -> {
      AdocFile adocFile = new AdocFile("hello sauerland!");
      adocFile.setContent(adocContent);
      session.persist(adocFile);
    });

    Path renderedFile = Paths.get(repoPath.toString(), AdocFile.class.getSimpleName(), "hello_sauerland.adoc");
    assertTrue(Files.exists(renderedFile));
  }

  @Test
  public void testPersistAndLoad() throws Exception {
    sessionFactory.transactedSession(session -> session.persist(new AdocFile("hello sauerland!").setContent(adocContent)));

    Path path = repository.getIndex().getAllOf(AdocFile.class).iterator().next().getPathInRepository();

    Path from = Paths.get(getClass().getResource("included.adoc").toURI());
    Path folder = path.getParent();
    Files.copy(from, folder.resolve(from.getFileName()));

    sessionFactory.transactedSession(session -> {
      String id = session.getRepository().getIndex().getAllOf(AdocFile.class).iterator().next().getId();
      AdocFile adocFile = session.findById(id);
      assertEquals(adocContent, adocFile.getContent());
      assertNotNull(adocFile.getPathInRepository());
      assertNotNull(adocFile.getLastModified());
      List<Path> includedFiles = adocFile.getIncludedFiles();
      assertNotNull(includedFiles);
      assertEquals(1, includedFiles.size());
      assertEquals("included.adoc", includedFiles.get(0).getFileName().toString());
    });
  }
}