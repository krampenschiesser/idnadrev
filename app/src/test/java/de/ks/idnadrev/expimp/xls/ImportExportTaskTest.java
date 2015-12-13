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
package de.ks.idnadrev.expimp.xls;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class ImportExportTaskTest {
  private static final Logger log = LoggerFactory.getLogger(ImportExportTaskTest.class);

  @Inject
  protected Cleanup cleanup;

  @Before
  public void setUp() throws Exception {
    cleanup.cleanup();
    createTaskExportData();
  }

  @Test
  public void testExportImportTasksWithCleanup() throws Exception {
    List<EntityExportSource<?>> sources = getSourcesToExport();

    File tempFile = File.createTempFile("taskExport", ".xlsx");
    XlsxExporter exporter = new XlsxExporter();
    exporter.export(tempFile, sources);

    cleanup.cleanup();

    XlsxImporter importer = new XlsxImporter();
    XlsxImportResultCollector results = importer.importFromFile(tempFile);
    log.info(results.describe());
    assertTrue(results.isSuccessful());

    PersistentWork.wrap(() -> {
      assertEntiyExists(Tag.class, "tag1");
      assertEntiyExists(Tag.class, "tag2");
      assertEntiyExists(Context.class, "context");
      assertEntiyExists(Task.class, "test");
      assertEntiyExists(Task.class, "parent");

      assertEquals("did not import the workunits", 2, PersistentWork.from(WorkUnit.class).size());
    });
    PersistentWork.wrap(() -> {
      Task task = PersistentWork.forName(Task.class, "test");
      assertNotNull("Parent not set", task.getParent());
      assertNotNull("Context not set", task.getContext());

      assertEquals("Tags not added to task", 2, task.getTags().size());
      assertEquals("WorkUnits not added to task", 2, task.getWorkUnits().size());
    });
  }

  @Test
  public void testExportImportTasksNoCleanup() throws Exception {
    List<EntityExportSource<?>> sources = getSourcesToExport();

    File tempFile = File.createTempFile("taskExport", ".xlsx");
    XlsxExporter exporter = new XlsxExporter();
    exporter.export(tempFile, sources);

    XlsxImporter importer = new XlsxImporter();
    importer.getImportCfg().keepExisting();

    XlsxImportResultCollector results = importer.importFromFile(tempFile);
    log.info(results.describe());
    assertTrue(results.isSuccessful());

    PersistentWork.wrap(() -> {
      assertEntiyExists(Tag.class, "tag1");
      assertEntiyExists(Tag.class, "tag2");
      assertEntiyExists(Context.class, "context");
      assertEntiyExists(Task.class, "test");
      assertEntiyExists(Task.class, "parent");

      assertEquals("did not import the workunits", 2, PersistentWork.from(WorkUnit.class).size());
    });
    PersistentWork.wrap(() -> {
      Task task = PersistentWork.forName(Task.class, "test");
      assertNotNull("Parent not set", task.getParent());
      assertNotNull("Context not set", task.getContext());

      assertEquals("Tags not added to task", 2, task.getTags().size());
      assertEquals("WorkUnits not added to task", 2, task.getWorkUnits().size());
    });
  }

  private void assertEntiyExists(Class<? extends NamedPersistentObject> clazz, String name) {
    NamedPersistentObject<?> object = PersistentWork.forName(clazz, name);
    assertNotNull(clazz.getSimpleName() + " with name='" + name + "' does not exist", object);
  }

  private List<EntityExportSource<?>> getSourcesToExport() {
    EntityExportSource<Task> taskSource = new EntityExportSource<>(PersistentWork.idsFrom(Task.class), Task.class);
    EntityExportSource<Context> contextSource = new EntityExportSource<>(PersistentWork.idsFrom(Context.class), Context.class);
    EntityExportSource<WorkUnit> workUnitSource = new EntityExportSource<>(PersistentWork.idsFrom(WorkUnit.class), WorkUnit.class);
    EntityExportSource<Tag> tagSource = new EntityExportSource<>(PersistentWork.idsFrom(Tag.class), Tag.class);
    return Arrays.asList(taskSource, contextSource, workUnitSource, tagSource);
  }

  private void createTaskExportData() {
    PersistentWork.run(em -> {
      Context context = new Context("context");
      Task parent = new Task("parent");

      Task task = new Task("test");
      task.setParent(parent);
      task.setContext(context);
      task.setDescription("desc");
      task.getMentalEffort().setAmount(5);
      task.getPhysicalEffort().setAmount(2);

      WorkUnit workUnit1 = new WorkUnit(task);
      workUnit1.setStart(LocalDateTime.now().minusHours(3));
      workUnit1.setEnd(LocalDateTime.now().minusHours(2));
      WorkUnit workUnit2 = new WorkUnit(task);
      workUnit2.setStart(LocalDateTime.now().minusHours(1));
      workUnit2.setEnd(LocalDateTime.now());

      Tag tag1 = new Tag("tag1");
      Tag tag2 = new Tag("tag2");
      task.getTags().add(tag1);
      task.getTags().add(tag2);

      em.persist(parent);
      em.persist(context);
      em.persist(task);
      em.persist(workUnit1);
      em.persist(workUnit2);
      em.persist(tag1);
      em.persist(tag2);
    });
  }
}
