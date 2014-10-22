/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.category.create;

import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public class ColorStringConverter extends StringConverter<Color> {
  @Override
  public String toString(Color clr) {
    return String.format("#%02X%02X%02X", (int) (clr.getRed() * 255), (int) (clr.getGreen() * 255), (int) (clr.getBlue() * 255));
  }

  @Override
  public Color fromString(String string) {
    if (string == null || string.trim().isEmpty()) {
      return Color.WHITE;
    }
    return Color.web(string);
  }
}
