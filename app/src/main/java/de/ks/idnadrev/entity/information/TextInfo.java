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

import de.ks.idnadrev.entity.FileContainer;
import de.ks.idnadrev.entity.FileReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@AssociationOverrides(@AssociationOverride(name = "tags", joinTable = @JoinTable(name = "textinfo_tag")))
public class TextInfo extends Information<TextInfo> implements FileContainer<TextInfo> {
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(name = "textinfo_file")
  protected Set<FileReference> files = new HashSet<>();

  protected TextInfo() {
  }

  public TextInfo(String name) {
    super(name);
  }

  @Override
  public Set<FileReference> getFiles() {
    return files;
  }

  @Override
  public String getDescription() {
    return getContent();
  }

  @Override
  public TextInfo setDescription(String description) {
    setContent(description);
    return this;
  }
}
