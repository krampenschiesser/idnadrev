/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileDeletionRunnable implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(FileDeletionRunnable.class);
  private File file;

  public FileDeletionRunnable(File file) {
    this.file = file;
  }

  @Override
  public void run() {
    if (file.exists()) {
      file.delete();
      log.info("Deleted file {}", file);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FileDeletionRunnable)) {
      return false;
    }

    FileDeletionRunnable that = (FileDeletionRunnable) o;

    if (file != null ? !file.equals(that.file) : that.file != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return file != null ? file.hashCode() : 0;
  }
}
