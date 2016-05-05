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

import de.ks.texteditor.render.RenderType;
import de.ks.texteditor.render.Renderer;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ViewTask implements Supplier<Path> {
  private final Path srcFile;
  private final Path renderTarget;
  private final Renderer renderer;

  public ViewTask(Path srcFile, Path renderTarget, Renderer renderer) {
    this.srcFile = srcFile;
    this.renderTarget = renderTarget;
    this.renderer = renderer;
  }

  @Override
  public Path get() {
    try {
      boolean canReuseTarget = canReuseTarget(renderTarget);
      if (canReuseTarget) {
        return renderTarget;
      } else {
        return renderer.renderFilePreview(srcFile, renderTarget, RenderType.HTML);
      }
    } catch (IOException e) {
      return renderTarget;
    }
  }

  protected boolean canReuseTarget(Path targetFile) throws IOException {
    if (!Files.exists(srcFile)) {
      return false;
    }
    Path md5File = srcFile.getParent().resolve(srcFile.getFileName().toString() + ".md5");
    String content = Files.readAllLines(srcFile, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n"));
    String md5Current = DigestUtils.md5Hex(content);
    boolean canReuseTarget = false;
    if (Files.exists(targetFile) && Files.exists(md5File)) {
      List<String> md5Lines = Files.readAllLines(md5File, StandardCharsets.US_ASCII);
      if (md5Lines.size() == 1) {
        String lastMd5 = md5Lines.get(0);
        canReuseTarget = md5Current.equals(lastMd5);
      }
    }
    return canReuseTarget;
  }
}
