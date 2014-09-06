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

import com.google.common.util.concurrent.MoreExecutors;
import de.ks.LauncherRunner;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class XlsxImporterTest {

  private File thoughtsFile;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(Thought.class);
    URL thoughtsFileUrl = getClass().getResource("thoughts.xlsx");
    assertNotNull(thoughtsFileUrl);
    thoughtsFile = new File(thoughtsFileUrl.toURI());
  }

  @Test
  public void testImportThought() throws Exception {
    XlsxImporter importer = new XlsxImporter(MoreExecutors.sameThreadExecutor());
    importer.importFromFile(thoughtsFile);

    List<Thought> thoughts = PersistentWork.from(Thought.class);
    assertEquals(142, thoughts.size());

    Thought first = PersistentWork.forName(Thought.class, "Thought000");
    assertNotNull(first);
    int nanos = (int) TimeUnit.MILLISECONDS.toNanos(578);

    assertEquals(LocalDateTime.of(2014, 9, 5, 19, 28, 34, nanos), first.getCreationTime());
    Thought last = PersistentWork.forName(Thought.class, "Thought141");
    assertNotNull(last);
    assertEquals(LocalDateTime.of(2014, 9, 5, 19, 28, 36), last.getCreationTime());
  }
}