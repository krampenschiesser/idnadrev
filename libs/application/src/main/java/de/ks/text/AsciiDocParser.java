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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.asciidoctor.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AsciiDocParser {
  private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
  private final Options options;
  private static final Pattern footerPattern = Pattern.compile("<div id=\"footer\">\n<div id=\"footer-text\">\n" +
          ".*\n" +
          "</div>\n</div>");
  private static final Pattern ascciiDocCssPattern = Pattern.compile("<link rel=\"stylesheet\" href=\"./asciidoctor.css\">");
  private static final Pattern coderayCssPattern = Pattern.compile("<link rel=\"stylesheet\" href=\"./asciidoctor-coderay.css\">");
  private final Map<String, String> cssCache = new ConcurrentHashMap<>();

  public AsciiDocParser() {
    Attributes attributes = AttributesBuilder.attributes()//
            .experimental(true).tableOfContents(true).sourceHighlighter("coderay").get();
    options = OptionsBuilder.options().headerFooter(true).attributes(attributes).get();
  }

  public String parse(String input) {
    String render = asciidoctor.render(input, options);
    render = inlineCss(render);
    render = removeFooter(render);
    return render;
  }

  private String removeFooter(String render) {
    return footerPattern.matcher(render).replaceFirst("");
  }

  protected String inlineCss(String input) {
    String asciidoc = getCssString("asciidoctor.css");
    String coderay = getCssString("asciidoctor-coderay.css");
    input = input.replace("<link rel=\"stylesheet\" href=\"./asciidoctor.css\">", asciidoc);
    input = input.replace("<link rel=\"stylesheet\" href=\"./asciidoctor-coderay.css\">", coderay);
    return input;
  }

  private String getCssString(String cssFile) {
    return cssCache.computeIfAbsent(cssFile, (fileName) -> {
      String beginTag = "<style type=\"text/css\">";
      String endTag = "</style>";

      StringBuilder asciiDocCss = new StringBuilder();
      asciiDocCss.append(beginTag).append("\n");

      appendCssFile(asciiDocCss, fileName);
      asciiDocCss.append(endTag);
      return asciiDocCss.toString();
    });
  }

  private void appendCssFile(StringBuilder builder, String cssFile) {
    URL asciiDoctorCss = getClass().getResource("/org/asciidoctor/" + cssFile);
    File file = new File(asciiDoctorCss.getFile());
    try {
      List<String> lines = Files.readLines(file, Charsets.US_ASCII);
      lines.forEach(line -> builder.append(line).append("\n"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
