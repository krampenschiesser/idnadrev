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

import java.util.Collections;
import java.util.List;

public interface StyleDetector {
  List<DetectionResult> detect(Line line);

  default List<DetectionResult> wholeLine(Line line, String styleClass) {
    return Collections.singletonList(DetectionResult.wholeLine(line, styleClass));
  }

  default DetectionResult inline(Line line, int begin, int end, String styleClass) {
    return DetectionResult.inline(line.getPositionInDocument() + begin, line.getPositionInDocument() + end, styleClass);
  }

  default List<DetectionResult> none() {
    return Collections.emptyList();
  }

  public static class DetectionResult {
    public static DetectionResult wholeLine(Line line, String styleClass) {
      return new DetectionResult(true, line.getStart(), line.getEnd(), styleClass);
    }

    public static DetectionResult inline(int begin, int end, String styleClass) {
      return new DetectionResult(begin, end, styleClass);
    }

    boolean wholeLine = false;
    int begin, end;
    String styleClass;

    DetectionResult(boolean wholeLine, int begin, int end, String styleClass) {
      this.wholeLine = wholeLine;
      this.begin = begin;
      this.end = end;
      this.styleClass = styleClass;
    }

    DetectionResult(int begin, int end, String styleClass) {
      this(false, begin, end, styleClass);
    }

    public boolean isWholeLine() {
      return wholeLine;
    }

    public int getBegin() {
      return begin;
    }

    public int getEnd() {
      return end;
    }

    public String getStyleClass() {
      return styleClass;
    }
  }
}
