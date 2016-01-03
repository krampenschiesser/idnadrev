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
package de.ks.texteditor.preview;

import de.ks.texteditor.render.Renderer;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class PreviewTask implements Callable<Path> {
  private final Path temporarySourceFile;
  private final Path targetDirectory;
  private final String content;
  private final Renderer renderer;

  public PreviewTask(Path temporarySourceFile, Path targetDirectory, String content, Renderer renderer) {
    this.temporarySourceFile = temporarySourceFile;
    this.targetDirectory = targetDirectory;
    this.content = content;
    this.renderer = renderer;
  }

  @Override
  public Path call() throws Exception {
    String md5 = DigestUtils.md5Hex(content);
    Files.write(temporarySourceFile, content.getBytes(StandardCharsets.UTF_8));
    return renderer.renderFilePreview(temporarySourceFile, targetDirectory);
  }
}
