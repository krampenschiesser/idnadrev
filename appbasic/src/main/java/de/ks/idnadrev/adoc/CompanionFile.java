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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CompanionFile {
  static Set<String> imageEndings = new HashSet<>(Arrays.asList(".jpg", ".png", ".tiff", ".bmp", ".jpeg"));
  protected Path path;
  protected String name;
  protected FileType fileType;

  public CompanionFile(Path path) {
    this.path = path;
    name = path.getFileName().toString();
    String lowerCaseName = name.toLowerCase(Locale.ROOT);
    if (lowerCaseName.contains(".")) {
      String fileEnding = lowerCaseName.substring(lowerCaseName.lastIndexOf("."));
      if (fileEnding.equals(".adoc")) {
        fileType = FileType.ADOC;
      } else if (fileEnding.equals(".csv")) {
        fileType = FileType.CSV;
      } else if (imageEndings.contains(fileEnding)) {
        fileType = FileType.IMAGE;
      } else {
        fileType = FileType.UNKNOWN;
      }
    } else {
      fileType = FileType.UNKNOWN;
    }
  }

}
