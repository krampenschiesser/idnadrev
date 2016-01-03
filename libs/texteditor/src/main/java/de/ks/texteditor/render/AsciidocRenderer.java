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

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;

import javax.inject.Inject;
import java.nio.file.Path;

public class AsciidocRenderer implements Renderer {

  private final Path cssPath;

  @Inject
  public AsciidocRenderer(Path cssPath) {
    this.cssPath = cssPath;
  }

  @Override
  public Path renderFilePreview(Path source, Path targetFile) {
    Asciidoctor asciidoctor = getAsciidoctor();
    OptionsBuilder optionsBuilder = getOptions(source, targetFile);

    asciidoctor.renderFile(source.toFile(), optionsBuilder);
    return targetFile;
  }

  protected OptionsBuilder getOptions(Path source, Path targetFile) {
    OptionsBuilder options = OptionsBuilder.options();
    options.toFile(targetFile.toFile());
    options.toDir(targetFile.getParent().toFile());
    options.baseDir(source.getParent().toFile());
    options.destinationDir(targetFile.getParent().toFile());
    options.backend("pdf");

    AttributesBuilder attributes = AttributesBuilder.attributes();
    attributes.linkCss(true);
    attributes.experimental(true);
    attributes.stylesDir(targetFile.relativize(cssPath).toString());
    attributes.attribute("stem");

    options.attributes(attributes);
    return options;
  }

  protected Asciidoctor getAsciidoctor() {
    return Asciidoctor.Factory.create();
  }

  @Override
  public String renderStringPreview(Path source) {
    return null;
  }
}
