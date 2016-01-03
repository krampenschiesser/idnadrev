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

package de.ks.texteditor.markup;

import java.util.ArrayList;
import java.util.List;

public class InlineStyle implements StyleDetector {
  private final String styleClass;
  private final String begin;
  private final String end;

  public InlineStyle(String beginChar, String endChar, String styleClass) {
    this.styleClass = styleClass;
    begin = beginChar;
    end = endChar;
  }

  @Override
  public List<DetectionResult> detect(Line line) {
    ArrayList<DetectionResult> retval = new ArrayList<>();

    int startPos = 0;
    int beginIndex = -1;
    int endIndex = -1;
    do {
      beginIndex = line.getText().indexOf(begin, startPos);
      if (beginIndex >= 0) {
        endIndex = line.getText().indexOf(end, beginIndex + 1);
        if (endIndex > beginIndex) {
          startPos = endIndex + 1;
          retval.add(inline(line, beginIndex, endIndex + 1, styleClass));
        }
      }
    } while (beginIndex >= 0 && endIndex > 0);
    return retval;
  }
}
