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
package de.ks.idnadrev.entity;

import java.io.IOException;
import java.util.Set;

public interface FileContainer {

  Set<FileReference> getFiles();

  void addFile(FileReference file);

  void removeFile(FileReference file);

  default void addFile(String fileName) throws IOException {
    addFile(new java.io.File(fileName));
  }

  default void addFile(java.io.File file) throws IOException {
    //FileReference noteFile = FileReference.fromFile(file);
    //addFile(noteFile);
  }

  default void removeFile(String fileName) throws IOException {
    removeFile(new java.io.File(fileName));
  }

  default void removeFile(java.io.File file) throws IOException {
    //FileReference noteFile = FileReference.fromFile(file);
    //removeFile(noteFile);
  }

}
