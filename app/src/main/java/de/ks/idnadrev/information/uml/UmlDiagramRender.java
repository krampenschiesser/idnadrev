/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.information.uml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;

public class UmlDiagramRender {
  private static final Logger log = LoggerFactory.getLogger(UmlDiagramRender.class);

  public File generatePng(String uml, double width, Path path) {
    StringBuilder builder = new StringBuilder();
    builder.append("@startuml\n");
    builder.append("scale ");
    builder.append((int) width);
    builder.append(" width \n");
    builder.append(uml);
    builder.append("\n@enduml");

    try {
      try (FileOutputStream outStream = new FileOutputStream(path.toFile())) {
        SourceStringReader reader = new SourceStringReader(builder.toString());
        FileFormatOption fileFormatOption = new FileFormatOption(FileFormat.PNG);
        String desc = reader.generateImage(outStream, fileFormatOption);

        log.info(desc);
      }
      return path.toFile();
    } catch (Exception e) {
      log.error("Could not create uml diagram", e);
      return null;
    }
  }
}
