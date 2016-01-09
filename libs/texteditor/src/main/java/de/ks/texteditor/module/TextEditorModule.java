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
package de.ks.texteditor.module;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import de.ks.standbein.javafx.FxCss;
import de.ks.texteditor.launch.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class TextEditorModule extends AbstractModule {
  private static final Logger log = LoggerFactory.getLogger(TextEditorModule.class);
  public static final String DATA_DIR = "dataDir";

  @Override
  protected void configure() {
    File dataDir = discoverDataDir();
    binder().bind(Key.get(Path.class, Names.named(DATA_DIR))).toInstance(dataDir.toPath());

    Multibinder.newSetBinder(binder(), String.class, FxCss.class).addBinding().toInstance("/de/ks/texteditor/markup/adoc/adoc.css");
  }

  public static File discoverDataDir() {
    File workingDirectory;
    String pathname = "data" + File.separator + MetaData.ADOC_CSS_ZIP;
    for (workingDirectory = new File(System.getProperty("user.dir")); !new File(workingDirectory, pathname).exists(); workingDirectory = workingDirectory.getParentFile()) {
    }
    File dir = new File(workingDirectory, "data");
    log.info("Discovered data dir {}", dir);
    return dir;
  }
}
