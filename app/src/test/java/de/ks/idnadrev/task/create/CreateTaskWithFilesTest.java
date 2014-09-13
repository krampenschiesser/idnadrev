/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.idnadrev.task.create;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.file.FileStore;
import de.ks.file.FileViewController;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.FileReference;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import de.ks.text.AsciiDocEditor;
import de.ks.util.FXPlatform;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(LauncherRunner.class)
public class CreateTaskWithFilesTest extends ActivityTest {
  @Inject
  FileStore fileStore;
  private MainTaskInfo controller;
  private CreateTask createTask;
  private AsciiDocEditor expectedOutcomeEditor;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return CreateTaskActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    em.persist(new Context("context"));
  }

  @Before
  public void setUp() throws Exception {
    createTask = activityController.<CreateTask>getCurrentController();
    controller = createTask.mainInfoController;
    expectedOutcomeEditor = createTask.expectedOutcomeController.expectedOutcome;
  }

  @Test
  public void testTaskFromThought() throws Exception {
    Thought bla = createThoughtAndTestFile();

    @SuppressWarnings("unchecked") CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    datasource.fromThought = bla;
    activityController.reload();
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertEquals("Bla", controller.name.getText());
    assertEquals("description", controller.description.getText());

    activityController.save();
    activityController.waitForTasks();

    assertNull(datasource.fromThought);
    assertEquals(0, PersistentWork.from(Thought.class).size());

    PersistentWork.wrap(() -> {
      List<FileReference> fileReferences = PersistentWork.from(FileReference.class);
      assertEquals(1, fileReferences.size());
      List<Task> tasks = PersistentWork.from(Task.class);
      assertEquals(1, tasks.size());
      Set<FileReference> files = tasks.get(0).getFiles();
      assertEquals(1, files.size());
      assertEquals(fileReferences.get(0), files.iterator().next());
    });
  }

  @Test
  public void testAddFile() throws Exception {
    createThoughtAndTestFile();

    @SuppressWarnings("unchecked") CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    activityController.reload();
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      FileViewController fileView = activityController.getControllerInstance(FileViewController.class);
      fileView.addFiles(Arrays.asList(getTestFile()));
    });

    activityController.save();
    Thread.sleep(100);
    activityController.waitForTasks();

    PersistentWork.wrap(() -> {
      List<FileReference> fileReferences = PersistentWork.from(FileReference.class);
      assertEquals(1, fileReferences.size());
      List<Task> tasks = PersistentWork.from(Task.class);
      assertEquals(1, tasks.size());
      Set<FileReference> files = tasks.get(0).getFiles();
      assertEquals(1, files.size());
      assertEquals(fileReferences.get(0), files.iterator().next());
    });
  }

  protected File getTestFile() {
    String fileStoreDir = fileStore.getFileStoreDir();
    File dir = new File(fileStoreDir + File.separator + "fileDir");
    File testFile = new File(dir, "test");
    return testFile;
  }

  protected Thought createThoughtAndTestFile() throws IOException {
    String fileStoreDir = fileStore.getFileStoreDir();
    File dir = new File(fileStoreDir + File.separator + "fileDir");
    if (!dir.exists()) {
      Files.createDirectories(dir.toPath());
    }
    File testFile = new File(dir, "test");
    if (!testFile.exists()) {
      testFile.createNewFile();
    }

    return PersistentWork.wrap(() -> {
      Thought thought = new Thought("Bla").setDescription("description");
      FileReference reference = new FileReference("test", "md5123");
      thought.addFileReference(reference);
      PersistentWork.persist(thought);
      PersistentWork.persist(reference);
      return thought;
    });
  }
}
