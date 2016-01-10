/*
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.texteditor.render;

import de.ks.texteditor.launch.AsciiDocParser;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AsciidocRenderer implements Renderer {
  public static final String NAME = "AsciiDoctor";
  private final AsciiDocParser parser;

  @Inject
  public AsciidocRenderer(AsciiDocParser parser) {
    this.parser = parser;
  }

  @Override
  public Path renderFilePreview(Path source, Path targetFile, RenderType type) {
    Asciidoctor asciidoctor = parser.getAsciidoctor();
    OptionsBuilder optionsBuilder = getOptions(source, targetFile, type);

    asciidoctor.renderFile(source.toFile(), optionsBuilder);
    return targetFile;
  }

  @Override
  public List<RenderType> getSupportedRenderingTypes() {
    return Arrays.asList(RenderType.values());
  }

  @Override
  public String getName() {
    return NAME;
  }

  protected OptionsBuilder getOptions(Path source, Path targetFile, RenderType type) {
    Path parent = targetFile.getParent();
    OptionsBuilder options = parser.getDefaultOptions();
    options.toFile(targetFile.toFile());
    options.toDir(parent.toFile());
    options.baseDir(source.getParent().toFile());
    options.destinationDir(parent.toFile());
    options.backend(type.name().toLowerCase(Locale.ROOT));
    return options;
  }

}