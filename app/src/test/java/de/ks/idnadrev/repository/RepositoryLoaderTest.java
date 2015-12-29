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

package de.ks.idnadrev.repository;

import com.google.common.base.StandardSystemProperty;
import de.ks.flatadocdb.Repository;
import de.ks.flatadocdb.util.DeleteDir;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RepositoryLoaderTest {

  private Path target;
  private String repo;
  private RepositoryLoader repositoryLoader;
  private String tmpDir;

  @Before
  public void setUp() throws Exception {
    tmpDir = StandardSystemProperty.JAVA_IO_TMPDIR.value();
    target = Paths.get(tmpDir, "repos");
    Path repoPath = Paths.get(tmpDir, "myIndex");
    if (Files.exists(repoPath)) {
      new DeleteDir(repoPath).delete();
    }
    Files.createDirectories(repoPath);
    repo = repoPath.toString();
    repositoryLoader = new RepositoryLoader(target);
  }

  @Test
  public void testLoadRepositories() throws Exception {
    Files.write(target, Arrays.asList("#bla", repo, "#blubb"));
    List<Repository> repositories = repositoryLoader.loadRepositories();
    assertEquals(1, repositories.size());
  }

  @Test
  public void testAddRepo() throws Exception {
    Files.write(target, Arrays.asList("#bla", repo, "#blubb"));
    Path otherRepo = Paths.get(tmpDir, "otherRepo");
    if (Files.exists(otherRepo)) {
      new DeleteDir(otherRepo).delete();
    }
    Files.createDirectories(otherRepo);

    repositoryLoader.addRepository(otherRepo);

    List<Repository> repositories = repositoryLoader.loadRepositories();
    assertEquals(2, repositories.size());
  }
}