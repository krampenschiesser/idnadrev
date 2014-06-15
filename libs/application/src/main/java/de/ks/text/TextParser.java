/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks.text;

import org.asciidoctor.*;

public class TextParser {
  Asciidoctor asciidoctor = Asciidoctor.Factory.create();

  public String parse(String input) {
    Attributes attributes = AttributesBuilder.attributes().linkCss(false).unsetStyleSheet().get();
    Options options = OptionsBuilder.options().headerFooter(true).attributes(attributes).get();//.docType("HTML")
    String render = asciidoctor.render(input, options);
    return render;
  }
}
