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
package de.ks.text;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class PersistentStoreBack {
  private static final Logger log = LoggerFactory.getLogger(PersistentStoreBack.class);
  protected final String id;
  protected final File targetDirectory;

  public PersistentStoreBack(String id, File targetDirectory) {
    this.id = id;
    this.targetDirectory = targetDirectory;
  }

  public synchronized void save(String text) {
    File file = new File(targetDirectory, id);
    if (file.exists()) {
      file.delete();
    }
    try {
      Files.createParentDirs(file);
      Files.write(text, file, Charsets.UTF_8);
    } catch (IOException e) {
      log.error("Could not write storeback to {}", file, e);
    }
  }

  public synchronized void delete() {
    File file = new File(targetDirectory, id);
    if (file.exists()) {
      file.delete();
    }
  }

  public synchronized String load() {
    File file = new File(targetDirectory, id);
    if (file.exists()) {
      try {
        StringBuilder builder = new StringBuilder();
        Files.readLines(file, Charsets.UTF_8).forEach(l -> builder.append(l).append("\n"));
        return builder.toString();
      } catch (IOException e) {
        log.error("Could not read from {}", file, e);
      }
    }
    return "";
  }

  public String getId() {
    return id;
  }

  public File getTargetDirectory() {
    return targetDirectory;
  }
}
