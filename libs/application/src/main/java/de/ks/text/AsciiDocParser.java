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
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Vetoed
public class AsciiDocParser {
  private static final Logger log = LoggerFactory.getLogger(AsciiDocParser.class);

  protected static final String mathJaxStart = "   <script type=\"text/x-mathjax-config\">\n" +
          "    MathJax.Hub.Config({\n" +
          "    asciimath2jax: {\n" +
          "    delimiters: [['`','`'], ['$$','$$'], ['||','||']]\n" +
          "    }\n" +
          "    });\n" +
          "    </script>\n<script type=\"text/javascript\"\n" +
          "  src=\"";
  protected static final String mathJaxEnd = "MathJax.js?config=AM_HTMLorMML-full\">\n" + "</script>";
  private static final Pattern footerPattern = Pattern.compile("<div id=\"footer\">\n<div id=\"footer-text\">\n" +
          ".*\n" +
          "</div>\n</div>");
  private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
  private final OptionsBuilder defaultOptions;
  private final Map<String, String> cssCache = new ConcurrentHashMap<>();
  private final File dataDir;
  private final AsciiDocMetaData metaData = new AsciiDocMetaData();

  public AsciiDocParser() {
    dataDir = new AsciiDocMetaData().disocverDataDir();
    defaultOptions = getDefaultOptions(getDefaultAttributes());
  }

  public AttributesBuilder getDefaultAttributes() {
    AttributesBuilder attributes = AttributesBuilder.attributes()//
            .experimental(true).sourceHighlighter("coderay").copyCss(true).stylesDir(dataDir.toURI().toString());
    return attributes;
  }

  public OptionsBuilder getDefaultOptions(AttributesBuilder attributes) {
    return OptionsBuilder.options().headerFooter(true).backend(AsciiDocBackend.HTML5.name().toLowerCase()).attributes(attributes.get());
  }

  public String parse(String input) {
    return parse(input, true, defaultOptions);
  }

  public String parse(String input, boolean removeFooter, OptionsBuilder options) {
    String render = asciidoctor.render(input, options);
    String backend = (String) options.asMap().get(Options.BACKEND);
    if (backend.equals(AsciiDocBackend.HTML5.name().toLowerCase())) {
      if (removeFooter) {
        render = removeFooter(render);
      }
      render = addMathJax(render);
    }
    return render;
  }

  public void renderToFile(AsciiDocBackend backend, File file) {
    if (file.exists()) {
      log.info("Removing existing render target {}", file);
    }
    String child = file.getName().contains(".") ? file.getName().substring(0, file.getName().lastIndexOf('.')) : file.getName();
    File dataDir = new File(file.getParent(), child + "_data");
    log.debug("using target data dir {}", dataDir);
  }

  private String addMathJax(String render) {
    int index = render.lastIndexOf("</head>");
    if (index > 0) {
      String first = render.substring(0, index);
      String last = render.substring(index);

      return first + mathJaxStart + new File(dataDir, "mathjax").toURI().toString() + File.separator + mathJaxEnd + last;
    }
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
