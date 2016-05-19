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

package de.ks.idnadrev.adoc.add;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.adoc.Header;
import de.ks.idnadrev.adoc.NameStripper;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.repository.ActiveRepository;
import de.ks.idnadrev.repository.Repository;
import de.ks.idnadrev.util.AdocFileCreateEditDs;

import javax.inject.Inject;
import javax.inject.Provider;

public class AddAdocDs extends AdocFileCreateEditDs<AdocFile> {
  @Inject
  public AddAdocDs(@ActiveRepository Provider<Repository> activeRepositoryProvider, NameStripper nameStripper, Index index) {
    super(AdocFile.class, "Text", "Text.adoc", p -> new AdocFile(p, null, new Header(null)), activeRepositoryProvider, nameStripper, index);
  }
}
