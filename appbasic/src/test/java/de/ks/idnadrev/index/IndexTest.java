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

package de.ks.idnadrev.index;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.adoc.Header;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class IndexTest {
  @Test
  public void querying() throws Exception {
    AdocFile adocFile1 = new AdocFile(Mockito.mock(Path.class), null, new Header(null).setTitle("test123"));
    AdocFile adocFile2 = new AdocFile(Mockito.mock(Path.class), null, new Header(null).setTitle("bla"));

    Index index = new Index(Collections.singleton(StandardQueries.titleQuery()));
    index.add(adocFile1);
    index.add(adocFile2);

    Collection<AdocFile> result = index.queryNonNull(AdocFile.class, StandardQueries.titleQuery(), title -> title.startsWith("test"));
    assertEquals(1, result.size());
  }

  @Test
  public void removeEntry() throws Exception {
    AdocFile adocFile1 = new AdocFile(Mockito.mock(Path.class), null, new Header(null).setTitle("test123"));
    Index index = new Index(Collections.singleton(StandardQueries.titleQuery()));
    index.add(adocFile1);


    Collection<AdocFile> result = index.queryNonNull(AdocFile.class, StandardQueries.titleQuery(), title -> title.startsWith("test"));
    assertEquals(1, result.size());

    index.remove(adocFile1);
    result = index.queryNonNull(AdocFile.class, StandardQueries.titleQuery(), title -> title.startsWith("test"));
    assertEquals(0, result.size());
  }
}