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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NewStyleHeader implements StyleDetector {

  public static final String ADOC_HEADER = "adocHeader";
  private final String headerStart;
  private final int headerLevel;

  public NewStyleHeader(int headerLevel) {
    this(headerLevel, "=");
  }

  protected NewStyleHeader(int headerLevel, String headerChar) {
    this.headerLevel = headerLevel;
    headerStart = IntStream.range(0, headerLevel).mapToObj(i -> headerChar).collect(Collectors.joining());
  }

  @Override
  public int detect(String line) {
    if (line.startsWith(headerStart + " ")) {
      return 0;
    } else {
      return -1;
    }
  }

  @Override
  public String getStyleClass() {
    return ADOC_HEADER + headerLevel;
  }
}
