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
package de.ks.texteditor.markup.adoc;

import de.ks.texteditor.markup.Line;
import de.ks.texteditor.markup.StyleDetector;

import java.util.List;

public class AdocTitleElementStyle implements StyleDetector {
  public static final String ADOC_TITLE_ELEMENT = "adocTitleElement";

  @Override
  public List<DetectionResult> detect(Line line) {
    String text = line.getText();
    if (text.startsWith(":") && (text.endsWith(":") || text.contains(": "))) {
      return wholeLine(line, ADOC_TITLE_ELEMENT);
    } else {
      return none();
    }
  }

}
