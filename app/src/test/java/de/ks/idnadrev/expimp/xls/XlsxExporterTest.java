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

import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.Cleanup;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.expimp.EntityExportSource;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class XlsxExporterTest {
  private static final Logger log = LoggerFactory.getLogger(XlsxExporterTest.class);
  public static final int COUNT = 142;

  @Before
  public void setUp() throws Exception {
    new Cleanup().cleanup();

    PersistentWork.run(em -> {
      for (int i = 0; i < COUNT; i++) {
        em.persist(new Thought(String.format("Thought%03d", i)));
      }
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

}
