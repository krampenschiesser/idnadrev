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
package de.ks.idnadrev.adoc;

import de.ks.idnadrev.index.Index;
import de.ks.util.DeleteDir;

import javax.inject.Inject;

public class AdocAccessor {
  private final Index index;

  @Inject
  public AdocAccessor(Index index) {
    this.index = index;
  }

  public void delete(AdocFile adocFile) {
    index.remove(adocFile);
    new DeleteDir(adocFile.getPath().getParent()).delete();
  }
}
