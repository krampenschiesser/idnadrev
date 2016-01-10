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

package de.ks.idnadrev.entity.information;

import de.ks.flatadocdb.annotation.Child;
import de.ks.flatadocdb.annotation.ToMany;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.TaggedEntity;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.adoc.AdocFile;
import de.ks.idnadrev.entity.adoc.AdocFileNameGenerator;
import de.ks.idnadrev.entity.adoc.SameFolderGenerator;

import java.util.Set;

public abstract class Information<E extends Information<E>> extends TaggedEntity {
  public static final String INFORMATION_TAG_JOINTABLE = "information_tag";

  private static final long serialVersionUID = 1L;

  @Child(fileGenerator = AdocFileNameGenerator.class, folderGenerator = SameFolderGenerator.class)
  protected AdocFile adocFile;

  @ToMany
  protected Set<Task> task;

  protected Information() {
    super(null);
  }

  public Information(String name) {
    super(name);
  }

  public String getContent() {
    return adocFile.getContent();
  }

  @SuppressWarnings("unchecked")
  public E setContent(String content) {
    adocFile.setContent(content);
    return (E) this;
  }

  public AdocFile getAdocFile() {
    return adocFile;
  }

  @Override
  public Set<Tag> getTags() {
    return tags;
  }
}