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
package de.ks.texteditor;

import com.google.common.base.StandardSystemProperty;
import de.ks.texteditor.markup.Line;

import java.util.ArrayList;
import java.util.List;

public class LineParser {
  String lineSeparator;

  public LineParser() {
    this(StandardSystemProperty.LINE_SEPARATOR.value());
  }

  public LineParser(String lineSeparator) {
    this.lineSeparator = lineSeparator;
  }

  public List<Line> getLines(String text) {
    int vagueLineLength = 120;
    int vagueLines = text.length() / vagueLineLength;
    ArrayList<Line> lines = new ArrayList<>(vagueLines < 10 ? 10 : vagueLineLength);

    char[] chars = text.toCharArray();
    StringBuilder line = new StringBuilder(vagueLineLength);
    int lastLineStart = 0;
    for (int i = 0; i < chars.length; i++) {
      char current = chars[i];
      if (lineSeparator.equals(String.valueOf(current))) {
        Line lastLine = new Line(lastLineStart, line.toString());
        lines.add(lastLine);
        lastLineStart = i + 1;
        line = new StringBuilder(vagueLineLength);
      } else {
        line.append(current);
      }
    }
    Line lastLine = new Line(lastLineStart, line.toString());
    lines.add(lastLine);
    return lines;
  }

}
