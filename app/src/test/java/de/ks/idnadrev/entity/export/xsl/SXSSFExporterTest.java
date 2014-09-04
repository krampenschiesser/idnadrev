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

package de.ks.idnadrev.entity.export.xsl;

import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.Thought;
import de.ks.idnadrev.entity.export.EntityExportSource;
import de.ks.persistence.PersistentWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RunWith(LauncherRunner.class)
public class SXSSFExporterTest {
  private static final Logger log = LoggerFactory.getLogger(SXSSFExporterTest.class);

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Thought.class);

    PersistentWork.run(em -> {
      for (int i = 0; i < 342; i++) {
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
  public void testExportThoughts() throws IOException {
    File tempFile = File.createTempFile("testExport", ".xlsx");
    EntityExportSource<Thought> source = new EntityExportSource<>(getAllIds(), Thought.class);
    SXSSFExporter exporter = new SXSSFExporter();
    exporter.export(tempFile, source);
  }
}
