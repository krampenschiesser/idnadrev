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

package de.ks.idnadrev.thought.add;

import com.google.common.base.StandardSystemProperty;
import de.ks.idnadrev.adoc.NameStripper;
import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.task.Task;
import de.ks.idnadrev.task.TaskState;
import de.ks.util.DeleteDir;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class AddThoughtDsTest {
  private Path tmp;
  private Path dsDir;
  private Path myRepo;
  private AddThoughtDs addThoughtDs;

  @Before
  public void setUp() throws Exception {
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

    addThoughtDs = new AddThoughtDs(() -> repository, new NameStripper());

  }

  @Test
  public void testCreateThought() throws Exception {
    Task task = addThoughtDs.loadModel(n -> "".toString());
    assertEquals(TaskState.UNPROCESSED, task.getState());
  }

  @Test
  public void testSaveThought() throws Exception {
    Task task = addThoughtDs.loadModel(n -> "".toString());
    task.setTitle("test");
    task.setContent("content");
    addThoughtDs.saveModel(task, n -> "".toString());

    Path targetFolder = myRepo.resolve("test");
    Path adocFilePath = targetFolder.resolve("Thought.adoc");
    assertTrue(Files.exists(adocFilePath));
    List<String> lines = Files.readAllLines(adocFilePath, StandardCharsets.UTF_8);
    assertTrue(lines.contains("= test"));
    assertTrue(lines.contains("content"));

  }
}