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

  default List<DetectionResult> wholeLine(Line line) {
    return Collections.singletonList(DetectionResult.wholeLine(line));
  }

  default DetectionResult inline(Line line, int begin, int end) {
    return DetectionResult.inline(line.getPositionInDocument() + begin, line.getPositionInDocument() + end);
  }

  default List<DetectionResult> none() {
    return Collections.emptyList();
  }

  String getStyleClass();

  public static class DetectionResult {
    public static DetectionResult wholeLine(Line line) {
      return new DetectionResult(true, line.getStart(), line.getEnd());
    }

    public static DetectionResult inline(int begin, int end) {
      return new DetectionResult(begin, end);
    }

    boolean wholeLine = false;
    int begin, end;

    DetectionResult(boolean wholeLine, int begin, int end) {
      this.wholeLine = wholeLine;
      this.begin = begin;
      this.end = end;
    }

    DetectionResult(int begin, int end) {
      this(false, begin, end);
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
  }
}
