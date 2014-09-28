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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class AsciiDocMetaData {
  private static final Logger log = LoggerFactory.getLogger(AsciiDocMetaData.class);
  public static final String MATHJAX = "mathjax";
  public static final String ASCIIDOCTOR_CSS = "asciidoctor.css";
  public static final String CODERAY_CSS = "asciidoctor-coderay.css";
  public static final String ADOC_CSS_ZIP = "adoc-css.zip";

  public static String DIR = null;
  private File dataDir;

  public void extract() {
    File dataDir = disocverDataDir();
    if (!new File(dataDir, MATHJAX).exists()) {
      new Unzipper(new File(dataDir, MATHJAX + ".zip")).unzip(dataDir);
    }
    if (!new File(dataDir, ASCIIDOCTOR_CSS).exists()) {
      new Unzipper(new File(dataDir, ADOC_CSS_ZIP)).unzip(dataDir);
    }
  }

  protected File disocverDataDir() {
    File workingDirectory;
    String pathname = "data" + File.separator + MATHJAX + ".zip";
    for (workingDirectory = new File(System.getProperty("user.dir")); !new File(workingDirectory, pathname).exists(); workingDirectory = workingDirectory.getParentFile()) {
    }
    File dir = new File(workingDirectory, "data");
    log.info("Discovered data dir {}", dir);
    return dir;
  }

  public File getDataDir() {
    if (dataDir == null) {
      dataDir = disocverDataDir();
    }
    return dataDir;
  }

  public void copyToDir(File newDataDir, boolean needsMathJax) {
    try {
      File[] files = getDataDir().listFiles();
      for (File file : files) {
        if (file.getName().equals(MATHJAX + ".zip")) {
          if (needsMathJax) {
            new Unzipper(file).unzip(newDataDir);
          }
        } else if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip") && !file.getName().equals(MATHJAX)) {
          Files.copy(file.toPath(), new File(newDataDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      }
    } catch (IOException e) {
      log.error("Could not copy to new data dir {}", dataDir, e);
      throw new RuntimeException(e);
    }
  }
}
