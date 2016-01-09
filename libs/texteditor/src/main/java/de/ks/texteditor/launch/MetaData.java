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
package de.ks.texteditor.launch;

import de.ks.texteditor.launch.util.Unzipper;
import de.ks.texteditor.module.TextEditorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Path;

public class MetaData {
  private static final Logger log = LoggerFactory.getLogger(MetaData.class);
  public static final String ASCIIDOCTOR_CSS = "asciidoctor.css";
  public static final String ADOC_CSS_ZIP = "adoc-css.zip";
  public static final String MARKDOWN_ZIP = "markdown.zip";
  public static final String MARKDOWN_CSS = "markdown.css";

  private final Path dataDir;

  @Inject
  public MetaData(@Named(TextEditorModule.DATA_DIR) Path dataDir) {
    this.dataDir = dataDir;
  }

  public void extract() {
    File dataDirFile = dataDir.toFile();
    if (!new File(dataDirFile, ASCIIDOCTOR_CSS).exists()) {
      new Unzipper(new File(dataDirFile, ADOC_CSS_ZIP)).unzip(dataDirFile);
    }
    if (!new File(dataDirFile, MARKDOWN_CSS).exists()) {
      new Unzipper(new File(dataDirFile, MARKDOWN_ZIP)).unzip(dataDirFile);
    }
  }

  public Path getDataDir() {
    return dataDir;
  }

}
