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
package de.ks.texteditor.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Markup {
  protected final List<StyleDetector> styleDetectors = new ArrayList<>();

  public abstract String getCssFile();

  public List<MarkupStyleRange> getStyleRanges(List<Line> lines) {
    ArrayList<MarkupStyleRange> retval = new ArrayList<>();
    final ArrayList<StyleDetector.DetectionResult> detectionResults = new ArrayList<>();
    final AtomicReference<StyleDetector> currentDetector = new AtomicReference<>(null);

    Runnable addMarkup = () -> {
      MarkupStyleRange lastWholeLine = null;
      for (StyleDetector.DetectionResult detectionResult : detectionResults) {
        if (detectionResult.isWholeLine()) {
          String styleName = detectionResult.getStyleClass();
          int begin = detectionResult.getBegin();
          int end = detectionResult.getEnd();

          if (lastWholeLine == null) {
            lastWholeLine = new MarkupStyleRange(begin, end, styleName);
            retval.add(lastWholeLine);
          } else {
            lastWholeLine.extendToPos(end);
          }
        } else {
          lastWholeLine = null;
          String styleName = detectionResult.getStyleClass();
          retval.add(new MarkupStyleRange(detectionResult.getBegin(), detectionResult.getEnd(), styleName));
        }
      }
      detectionResults.clear();
      currentDetector.set(null);
    };

    for (Line line : lines) {
      if (currentDetector.get() != null) {
        List<StyleDetector.DetectionResult> current = currentDetector.get().detect(line);
        if (current.size() > 0) {
          detectionResults.addAll(current);
          continue;
        } else {
          addMarkup.run();
        }
      }

      for (StyleDetector styleDetector : styleDetectors) {
        List<StyleDetector.DetectionResult> current = styleDetector.detect(line);
        if (current.size() > 0) {
          detectionResults.addAll(current);
          boolean isWholeLineResult = current.stream().filter(r -> r.isWholeLine()).count() == current.size();
          if (isWholeLineResult) {
            currentDetector.set(styleDetector);
            break;
          }
        }
      }
    }
    if (!detectionResults.isEmpty()) {
      addMarkup.run();
    }
    return retval;
  }
}
