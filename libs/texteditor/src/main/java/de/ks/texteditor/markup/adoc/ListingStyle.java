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
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ListingStyle implements StyleDetector {

  public static final String ADOC_LISTING = "adocListing";
  private final String listingChar;
  private final String styleSuffix;

  public ListingStyle(String listingChar, String styleSuffix) {
    this.listingChar = listingChar;
    this.styleSuffix = styleSuffix;
  }

  @Override
  public List<DetectionResult> detect(Line line) {
    if (line.getText().startsWith(listingChar)) {
      if (listingChar.endsWith(" ")) {
        return wholeLine(line, ADOC_LISTING + styleSuffix);
      } else {
        if (StringUtils.remove(line.getText(), listingChar).startsWith(" ")) {
          return wholeLine(line, ADOC_LISTING + styleSuffix);
        } else {
          return none();
        }
      }
    } else {
      return none();
    }
  }
}
