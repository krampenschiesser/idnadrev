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

import de.ks.texteditor.markup.InlineStyle;
import de.ks.texteditor.markup.LineBeginsStyle;
import de.ks.texteditor.markup.Markup;

public class AdocMarkup extends Markup {
  public AdocMarkup() {
    for (int header = 1; header <= 6; header++) {
      styleDetectors.add(new NewStyleHeader(header));
      styleDetectors.add(new MarkDownStyleHeader(header));
    }
    styleDetectors.add(new AdocBlockStyle());
    styleDetectors.add(new LineBeginsStyle("image::", "adocImage"));
    styleDetectors.add(new LineBeginsStyle("video::", "adocVideo"));
    styleDetectors.add(new AdocTitleElementStyle());
    styleDetectors.add(new ListingStyle(".", "Ordered"));
    styleDetectors.add(new ListingStyle("*", "Unordered"));
    styleDetectors.add(new ListingStyle("- ", "Unordered"));
    styleDetectors.add(new InlineStyle("*", "*", "adocBold"));
    styleDetectors.add(new InlineStyle("_", "_", "adocItalic"));
    styleDetectors.add(new InlineStyle("link:", "]", "adocLink"));
    styleDetectors.add(new InlineStyle("http://", "]", "adocLink"));
    styleDetectors.add(new InlineStyle("image::", "]", "adocImage"));
    styleDetectors.add(new InlineStyle("<<", ">>", "adocItalic"));
    styleDetectors.add(new InlineStyle("video::", "]", "adocVideo"));

  }

  @Override
  public String getCssFile() {
    return "/de/ks/texteditor/markup/adoc/adoc.css";
  }
}
