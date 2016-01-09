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

package de.ks.texteditor.render;

import com.google.common.base.StandardSystemProperty;
import de.ks.texteditor.launch.AsciiDocParser;
import de.ks.texteditor.module.TextEditorModule;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.*;

public class AsciidocRendererTest {

  private Path sourceFile;
  private Path targetFile;

  @Before
  public void setUp() throws Exception {
    String tmpDir = StandardSystemProperty.JAVA_IO_TMPDIR.value();
    Path sourceDir = Paths.get(tmpDir, getClass().getSimpleName());

    URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
    Path folder = Paths.get(location.toURI());
    Path parent = folder.getParent();
    for (; !parent.getFileName().toString().equals("libs"); parent = parent.getParent()) {
      //ok
    }
    Path userGuide = parent.getParent().resolve("doc").resolve("src").resolve("asciidoc").resolve("userguide.adoc");
    assertTrue(Files.exists(userGuide));

    Path userGuideDirectory = userGuide.getParent();

    Files.walkFileTree(userGuideDirectory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path relativize = userGuideDirectory.relativize(file);
        Path resolve = sourceDir.resolve(relativize);
        Files.copy(file, resolve, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path relativize = userGuideDirectory.relativize(dir);
        Path resolve = sourceDir.resolve(relativize);
        Files.createDirectories(resolve);
        return FileVisitResult.CONTINUE;
      }
    });
    Files.createDirectories(sourceDir);
    sourceFile = sourceDir.resolve(userGuideDirectory.relativize(userGuide));
    targetFile = sourceDir.resolve("render.html");

  }

  @Test
  public void testRenderFile() throws Exception {
    AsciidocRenderer asciidocRenderer = new AsciidocRenderer(new AsciiDocParser(TextEditorModule.discoverDataDir().toPath()));
    Path result = asciidocRenderer.renderFilePreview(sourceFile, targetFile, RenderType.HTML);
    assertNotNull(result);
    assertTrue(Files.exists(result));
  }
}