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

import de.ks.texteditor.markup.StyleDetector;

public class AdocBlockStyle implements StyleDetector {
  public static final String ADOC_BLOCK = "adocBlock";
  String detectedBlock;
  boolean stopDetectionOnNextLine = false;

  @Override
  public int detect(String line) {
    boolean isBlockStart = detectedBlock == null && line.startsWith("---");
    boolean isBlockEnd = detectedBlock != null && line.equals(detectedBlock);

    if (isBlockStart) {
      detectedBlock = line;
    } else if (isBlockEnd) {
      stopDetectionOnNextLine = true;
      return 0;
    }

    if (stopDetectionOnNextLine) {
      detectedBlock = null;
      stopDetectionOnNextLine = false;
      return -1;
    } else if (detectedBlock != null) {
      return 0;
    } else {
      return -1;
    }
  }

  @Override
  public String getStyleClass() {
    return ADOC_BLOCK;
  }
}
