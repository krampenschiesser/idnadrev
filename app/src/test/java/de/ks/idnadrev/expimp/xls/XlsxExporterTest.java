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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.sql.Timestamp;
import java.util.*;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class XlsxExporterTest {
  private static final Logger log = LoggerFactory.getLogger(XlsxExporterTest.class);
  public static final int COUNT = 142;
  @Inject
  protected Cleanup cleanup;

  @Before
  public void setUp() throws Exception {
    cleanup.cleanup();

    PersistentWork.run(em -> {
      for (int i = 0; i < COUNT; i++) {
        em.persist(new Thought(String.format("Thought%03d", i)));
      }
      Tag tag1 = new Tag("tag" + ToManyColumn.SEPARATOR + "1");
      Tag tag2 = new Tag("tag2");
      Task testTask = new Task("testTask");
      testTask.addTag(tag1);
      testTask.addTag(tag2);
      em.persist(testTask);
    });
  }

  protected List<Long> getAllIds() {
    return PersistentWork.read(em -> {
      CriteriaQuery<Long> criteriaQuery = em.getCriteriaBuilder().createQuery(Long.class);
      Root<Thought> root = criteriaQuery.from(Thought.class);
      Path<Long> id = root.<Long>get("id");
      criteriaQuery.select(id);
      return em.createQuery(criteriaQuery).getResultList();
    });
  }

  @Test
  public void testExportThoughts() throws Exception {
    File tempFile = File.createTempFile("thoughtExport", ".xlsx");
    EntityExportSource<Thought> source = new EntityExportSource<>(getAllIds(), Thought.class);
    XlsxExporter exporter = new XlsxExporter();
    exporter.export(tempFile, source);


    Workbook wb = WorkbookFactory.create(tempFile);
    Sheet sheet = wb.getSheetAt(0);
    assertEquals(Thought.class.getName(), sheet.getSheetName());
    int lastRowNum = sheet.getLastRowNum();
    assertEquals(COUNT, lastRowNum);
    Row firstRow = sheet.getRow(0);


    ArrayList<String> titles = new ArrayList<>();
    firstRow.cellIterator().forEachRemaining(col -> titles.add(col.getStringCellValue()));
    assertThat(titles.size(), greaterThanOrEqualTo(3));
    log.info("Found titles {}", titles);

    String creationTime = PropertyPath.property(Thought.class, t -> t.getCreationTime());
    String name = PropertyPath.property(Thought.class, t -> t.getName());
    String description = PropertyPath.property(Thought.class, t -> t.getDescription());

    assertTrue(titles.contains(creationTime));
    assertTrue(titles.contains(name));
    assertTrue(titles.contains(description));

    int nameColumn = titles.indexOf(name);
    ArrayList<String> names = new ArrayList<String>(COUNT);
    for (int i = 1; i <= COUNT; i++) {
      Row row = sheet.getRow(i);
      names.add(row.getCell(nameColumn).getStringCellValue());
    }
    Collections.sort(names);
    assertEquals("Thought000", names.get(0));
    assertEquals("Thought141", names.get(COUNT - 1));


    Date excelDate = sheet.getRow(1).getCell(titles.indexOf(creationTime)).getDateCellValue();

    Thought thought = PersistentWork.forName(Thought.class, "Thought000");

    Timestamp timestamp = java.sql.Timestamp.valueOf(thought.getCreationTime());
    Date creationDate = new Date(timestamp.getTime());
    assertEquals(creationDate, excelDate);
  }

  @Test
  public void testExportToManyRelation() throws Exception {
    File tempFile = File.createTempFile("taskExportTest", ".xlsx");
    EntityExportSource<Task> tasks = new EntityExportSource<>(PersistentWork.idsFrom(Task.class), Task.class);
    EntityExportSource<Tag> tags = new EntityExportSource<>(PersistentWork.idsFrom(Tag.class), Tag.class);
    XlsxExporter exporter = new XlsxExporter();
    exporter.export(tempFile, tasks, tags);


    Workbook wb = WorkbookFactory.create(tempFile);
    Sheet taskSheet = wb.getSheet(Task.class.getName());
    Sheet tagSheet = wb.getSheet(Tag.class.getName());
    assertNotNull(taskSheet);
    assertNotNull(tagSheet);

    Row firstRow = taskSheet.getRow(0);
    int pos = 0;
    Iterator<Cell> cellIterator = firstRow.cellIterator();

    String property = PropertyPath.property(Task.class, t -> t.getTags());
    while (cellIterator.hasNext()) {
      Cell cell = cellIterator.next();
      if (cell.getStringCellValue().equals(property)) {
        break;
      }
      pos++;
    }
    assertNotEquals(Task.class.getSimpleName() + "." + property + " not exported", firstRow.getLastCellNum(), pos);

    Cell cell = taskSheet.getRow(1).getCell(pos);
    String[] split = StringUtils.split(cell.getStringCellValue(), ToManyColumn.SEPARATOR);
    assertEquals(2, split.length);
    assertTrue(Arrays.asList(split).contains("tag" + ToManyColumn.SEPARATOR_REPLACEMENT + "1"));
    assertTrue(Arrays.asList(split).contains("tag2"));
  }
}
