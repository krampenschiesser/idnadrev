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

import de.ks.texteditor.module.TextEditorModule;
import de.ks.texteditor.render.RenderType;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Locale;

@Singleton
public class AsciiDocParser {
  protected final ThreadLocal<Asciidoctor> asciidoctor = ThreadLocal.withInitial(() -> {
    synchronized (AsciiDocParser.class) {
      return Asciidoctor.Factory.create();
    }
  });
  private final Path dataDir;
  private final OptionsBuilder defaultOptions;

  @Inject
  public AsciiDocParser(@Named(TextEditorModule.DATA_DIR) Path dataDir) {
    this.dataDir = dataDir;
    defaultOptions = getDefaultOptions(getDefaultAttributes());
  }

  public AttributesBuilder getDefaultAttributes() {
    AttributesBuilder attributes = AttributesBuilder.attributes()//
      .experimental(true)//
      .sourceHighlighter("coderay")//
      .linkCss(true)//
      .experimental(true)//
      .stylesDir(dataDir.toUri().toString());
    return attributes;
  }

  public OptionsBuilder getDefaultOptions(AttributesBuilder attributes) {
    return OptionsBuilder.options().headerFooter(true).backend(RenderType.HTML.name().toLowerCase(Locale.ROOT)).attributes(attributes.get()).safe(SafeMode.UNSAFE);
  }

  public Asciidoctor getAsciidoctor() {
    return asciidoctor.get();
  }

  public Path getDataDir() {
    return dataDir;
  }

  public OptionsBuilder getDefaultOptions() {
    return defaultOptions;
  }
}
