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

public class ImageStyle implements StyleDetector {

  public static final String ADOC_IMAGE = "adocImage";

  @Override
  public int detect(String line) {
    return line.indexOf("image::");
  }

  @Override
  public String getStyleClass() {
    return ADOC_IMAGE;
  }
}
