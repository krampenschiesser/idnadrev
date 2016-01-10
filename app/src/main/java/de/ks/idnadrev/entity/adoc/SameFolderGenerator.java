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
package de.ks.idnadrev.entity.adoc;

import de.ks.flatadocdb.Repository;
import de.ks.flatadocdb.defaults.JoinedRootFolderGenerator;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Mainly used for mapping children
 */
public class SameFolderGenerator extends JoinedRootFolderGenerator {
  @Override
  public Path getFolder(Repository repository, @Nullable Path path, Object o) {
    if (path == null) {
      return super.getFolder(repository, path, o);
    } else {
      return path;
    }
  }
}
