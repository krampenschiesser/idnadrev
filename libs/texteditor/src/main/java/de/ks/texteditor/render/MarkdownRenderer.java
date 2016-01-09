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

import de.ks.texteditor.launch.MarkdownParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownRenderer implements Renderer {
  private static final Logger log = LoggerFactory.getLogger(MarkdownRenderer.class);
  private final MarkdownParser parser;

  @Inject
  public MarkdownRenderer(MarkdownParser parser) {
    this.parser = parser;
  }

  @Override
  public Path renderFilePreview(Path source, Path targetFile, RenderType renderType) {
    String parse = parser.parse(source.toFile());
    try {
      Files.write(targetFile, parse.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      log.error("Could not write {}", targetFile, e);
    }
    return targetFile;
  }
}
