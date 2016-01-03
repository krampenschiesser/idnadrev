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

import de.ks.texteditor.markup.Markup;
import de.ks.texteditor.markup.StyleDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class AdocMarkup implements Markup {
  protected final List<StyleDetector> styleDetectors = new ArrayList<>();

  public AdocMarkup() {
    for (int header = 1; header <= 6; header++) {
      styleDetectors.add(new NewStyleHeader(header));
      styleDetectors.add(new MarkDownStyleHeader(header));
    }
    styleDetectors.add(new AdocBlockStyle());
    styleDetectors.add(new ImageStyle());
    styleDetectors.add(new AdocTitleElementStyle());
    styleDetectors.add(new ListingStyle(".", "Ordered"));
    styleDetectors.add(new ListingStyle("*", "Unordered"));
    styleDetectors.add(new ListingStyle("- ", "Unordered"));
  }

  @Override
  public String getCssFile() {
    return "/de/ks/texteditor/markup/adoc/adoc.css";
  }

  @Override
  public List<MarkupStyleRange> getStyleRanges(List<Line> lines) {
    ArrayList<MarkupStyleRange> retval = new ArrayList<>();
    final AtomicInteger lastDetection = new AtomicInteger(-1);
    final AtomicReference<StyleDetector> currentDetector = new AtomicReference<>(null);
    ArrayList<Line> currentLines = new ArrayList<>();

    BiConsumer<Line, Integer> onDetect = (line, detected) -> {
      boolean didInclude = includePreviousLines(lines, currentDetector.get(), currentLines, line);
      currentLines.add(line);
      if (didInclude) {
        lastDetection.set(currentLines.get(0).getPositionInDocument());
      } else {
        lastDetection.set(detected);
      }
    };

    Runnable addMarkup = () -> {
      String styleName = currentDetector.get().getStyleClass();
      int begin = lastDetection.get() > 0 ? lastDetection.get() : currentLines.get(0).getPositionInDocument();
      int end = currentLines.get(currentLines.size() - 1).getEnd();
      retval.add(new MarkupStyleRange(begin, end, styleName));
      lastDetection.set(-1);
      currentDetector.set(null);
      currentLines.clear();
    };

    for (Line line : lines) {
      if (currentDetector.get() != null) {
        int detected = currentDetector.get().detect(line.getText());
        if (detected >= 0) {
          onDetect.accept(line, detected);
          continue;
        } else {
          addMarkup.run();
        }
      }

      for (StyleDetector styleDetector : styleDetectors) {
        int detected = styleDetector.detect(line.getText());
        if (detected >= 0) {
          currentDetector.set(styleDetector);
          onDetect.accept(line, detected);
          break;
        }
      }
    }
    if (currentDetector.get() != null) {
      addMarkup.run();
    }
    return retval;
  }

  protected boolean includePreviousLines(List<Line> lines, StyleDetector currentDetector, ArrayList<Line> currentLines, Line line) {
    if (currentDetector.getPreviouslines() > 0) {
      int lineIndex = lines.indexOf(line);
      for (int i = Math.max(0, lineIndex - currentDetector.getPreviouslines()); i < lineIndex; i++) {
        currentLines.add(lines.get(i));
      }
      return true;
    } else {
      return false;
    }
  }
}
