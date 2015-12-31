/*
 * Copyright [2015] [Christian Loehnert]
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

package de.ks.idnadrev.entity;

import de.ks.flatadocdb.Repository;
import de.ks.flatjsondb.PersistentWork;
import de.ks.flatjsondb.RepositorySelector;
import de.ks.idnadrev.IdnadrevIntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ThoughtTest {
  private static final Logger log = LoggerFactory.getLogger(ThoughtTest.class);
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IdnadrevIntegrationTestModule()).launchServices();

  @Inject
  PersistentWork persistentWork;
  @Inject
  RepositorySelector repositorySelector;

  @Test
  public void testPersistThoughtWithFile() throws Exception {
    Thought test = new Thought("test");
    Path fileToCopy = Paths.get(getClass().getResource("img.jpg").toURI());
    test.addFile(fileToCopy);
    test.setDescription("= title\n= section\nhello sauerland!\nimage::img.jpg[bla]");

    persistentWork.persist(test);

    Repository repository = repositorySelector.getCurrentRepository();

    Path folder = repository.getPath().resolve(Thought.class.getSimpleName()).resolve("test");
    assertTrue(Files.exists(folder));

    Path json = folder.resolve("test.json");
    Path adoc = folder.resolve("test.adoc");
    Path img = folder.resolve("test.adoc");
    assertTrue(Files.exists(json));
    assertTrue(Files.exists(adoc));
    assertTrue(Files.exists(img));

    Thought thought = persistentWork.byName(Thought.class, "test");
    assertEquals(1, thought.getFiles().size());
  }
}
