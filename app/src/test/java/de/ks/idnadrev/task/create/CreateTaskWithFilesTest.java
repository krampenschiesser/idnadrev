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
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityStore;
import de.ks.file.FileStore;
import de.ks.file.FileViewController;
import de.ks.idnadrev.entity.*;
import de.ks.persistence.PersistentWork;
import de.ks.persistence.entity.Sequence;
import de.ks.text.AsciiDocEditor;
import de.ks.util.FXPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class CreateTaskWithFilesTest {
  @Inject
  ActivityController activityController;
  @Inject
  ActivityStore store;
  @Inject
  FileStore fileStore;
  private MainTaskInfo controller;
  private CreateTask createTask;
  private AsciiDocEditor expectedOutcomeEditor;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(FileReference.class, Sequence.class, WorkUnit.class, Task.class, Context.class, Tag.class, Thought.class);
    PersistentWork.persist(new Context("context"));

    activityController.start(CreateTaskActivity.class);
    activityController.waitForDataSource();
    createTask = activityController.<CreateTask>getCurrentController();
    controller = createTask.mainInfoController;
    expectedOutcomeEditor = createTask.expectedOutcomeController.expectedOutcome;
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(CreateTaskActivity.class);
  }

  @Test
  public void testTaskFromThought() throws Exception {
    Thought bla = createThoughtAndTestFile();

    @SuppressWarnings("unchecked") CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    datasource.fromThought = bla;
    activityController.reload();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();
    assertEquals("Bla", controller.name.getText());
    assertEquals("description", controller.description.getText());

    activityController.save();
    activityController.waitForDataSource();

    assertNull(datasource.fromThought);
    assertEquals(0, PersistentWork.from(Thought.class).size());

    PersistentWork.wrap(() -> {
      List<FileReference> fileReferences = PersistentWork.from(FileReference.class);
      assertEquals(1, fileReferences.size());
      assertNull(fileReferences.get(0).getThought());
      Task task = fileReferences.get(0).getTask();
      assertNotNull(task);
      assertEquals("fileDir", task.getFileStoreDir());
    });
  }

  @Test
  public void testAddFile() throws Exception {
    createThoughtAndTestFile();
    PersistentWork.deleteAllOf(FileReference.class);

    @SuppressWarnings("unchecked") CreateTaskDS datasource = (CreateTaskDS) store.getDatasource();
    activityController.reload();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();
    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      FileViewController fileView = activityController.getControllerInstance(FileViewController.class);
      fileView.addFiles(Arrays.asList(getTestFile()));
    });

    activityController.save();
    Thread.sleep(100);
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      List<FileReference> fileReferences = PersistentWork.from(FileReference.class);
      assertEquals(1, fileReferences.size());
      assertNull(fileReferences.get(0).getThought());
      Task task = fileReferences.get(0).getTask();
      assertNotNull(task);
      long seqNr = PersistentWork.from(Sequence.class).get(0).getSeqNr();
      String dir = String.format("%09d", seqNr);
      assertEquals(dir, task.getFileStoreDir());
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
      thought.setFileStoreDir("fileDir");
      FileReference reference = new FileReference("test", "md5123");
      reference.setFileStorePath("fileDir" + File.separator + "test");
      PersistentWork.persist(thought);
      reference.setOwner(thought);
      PersistentWork.persist(reference);
      return thought;
    });
  }
}
