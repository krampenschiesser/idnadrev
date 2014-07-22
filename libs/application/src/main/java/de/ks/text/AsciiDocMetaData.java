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

import de.ks.zip.Unzipper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AsciiDocMetaData {
  private static final Logger log = LoggerFactory.getLogger(AsciiDocMetaData.class);

  public static String DIR = null;

  public void extract() {
    File dataDir = disocverDataDir();
    if (!new File(dataDir, "mathjax").exists()) {
      new Unzipper(new File(dataDir, "mathjax.zip")).unzip(dataDir);
    }
    if (!new File(dataDir, "asciidoctor.css").exists()) {
      new Unzipper(new File(dataDir, "adoc-css.zip")).unzip(dataDir);
    }
  }

  protected File disocverDataDir() {
    File workingDirectory;
    String pathname = "data" + File.separator + "mathjax.zip";
    for (workingDirectory = new File(System.getProperty("user.dir")); !new File(workingDirectory, pathname).exists(); workingDirectory = workingDirectory.getParentFile()) {
    }
    File dir = new File(workingDirectory, "data");
    log.info("Discovered data dir {}", dir);
    return dir;
  }

}
