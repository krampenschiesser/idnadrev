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
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Vetoed;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
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
  public static final String DATADIR_NAME = "_data";
  private final ThreadLocal<Asciidoctor> asciidoctor = ThreadLocal.withInitial(() -> {
    synchronized (AsciiDocParser.class) {
      return Asciidoctor.Factory.create();
    }
  });
  private final OptionsBuilder defaultOptions;
  private final Map<String, String> cssCache = new ConcurrentHashMap<>();
  private final File dataDir;
  private final AsciiDocMetaData metaData = new AsciiDocMetaData();

  public AsciiDocParser() {
    dataDir = new AsciiDocMetaData().disocverDataDir();
    defaultOptions = getDefaultOptions(getDefaultAttributes());
    asciidoctor.get();
  }

  public AttributesBuilder getDefaultAttributes() {
    AttributesBuilder attributes = AttributesBuilder.attributes()//
      .experimental(true).sourceHighlighter("coderay").copyCss(true).stylesDir(dataDir.toURI().toString());
    return attributes;
  }

  public OptionsBuilder getDefaultOptions(AttributesBuilder attributes) {
    return OptionsBuilder.options().headerFooter(true).backend(AsciiDocBackend.HTML5.name().toLowerCase(Locale.ENGLISH)).attributes(attributes.get());
  }

  public String parse(String input) {
    try {
      String mathjaxDir = new File(dataDir, "mathjax").toURI().toString() + File.separator;
      return parse(input, true, true, mathjaxDir, defaultOptions);
    } catch (Exception e) {
      log.error("Got error", e);
      throw e;
    }
  }

  public String parse(String input, boolean removeFooter, boolean addMathJax, String mathjaxDir, OptionsBuilder options) {
    String render = asciidoctor.get().render(input, options);
    String backend = (String) options.asMap().get(Options.BACKEND);
    if (backend.equals(AsciiDocBackend.HTML5.name().toLowerCase(Locale.ENGLISH))) {
      if (removeFooter) {
        render = removeFooter(render);
      }
      if (addMathJax) {
        render = addMathJax(render, mathjaxDir);
      }
    }
    return render;
  }

  public void renderToFile(String input, AsciiDocBackend backend, File file) {
    if (file.exists()) {
      log.info("Removing existing render target {}", file);
      file.delete();
    }
    File dataDir = createDataDir(file);
    String mathjaxDir = "./" + dataDir.getName() + "/" + AsciiDocMetaData.MATHJAX + "/";
    boolean needsMathJax = needsMathJax(input);
    metaData.copyToDir(dataDir, needsMathJax);

    AttributesBuilder attributes = getDefaultAttributes();
    attributes.stylesDir(dataDir.getName());
    attributes.tableOfContents(true);
    attributes.imagesDir(dataDir.getName());

    OptionsBuilder options = getDefaultOptions(attributes);
    options.backend(backend.name().toLowerCase(Locale.ENGLISH));

    String parse = parse(input, false, needsMathJax, mathjaxDir, options);
    try {
      parse = copyFiles(parse, dataDir);
      Files.write(parse, file, Charsets.UTF_8);
    } catch (IOException e) {
      log.error("Could not write to file {}", file, e);
      throw new RuntimeException(e);
    }
  }

  private String copyFiles(String parse, File dataDir) throws IOException {
    Pattern pattern = Pattern.compile("\"file:.*\"");
    Matcher matcher = pattern.matcher(parse);
    int bodyTag = parse.indexOf("<body");
    Map<String, String> replacements = new HashMap<>();
    while (matcher.find()) {
      int start = matcher.start();
      if (start < bodyTag) {
        continue;
      }
      int end = matcher.end();

      String fileReference = parse.substring(start + 1, end - 1);
      end = fileReference.indexOf("\"");
      fileReference = fileReference.substring(0, end);

      log.debug("Found file reference {}", fileReference);

      URI uri = URI.create(fileReference.replace('\\', '/'));
      File sourceFile = new File(uri);
      File targetFile = new File(dataDir, sourceFile.getName());
      java.nio.file.Files.copy(sourceFile.toPath(), targetFile.toPath());

      replacements.put(fileReference, dataDir.getName() + "/" + targetFile.getName());
    }

    for (Map.Entry<String, String> entry : replacements.entrySet()) {
      String original = entry.getKey();
      String replacement = entry.getValue();
      parse = StringUtils.replace(parse, original, replacement);
    }
    return parse;
  }

  protected boolean needsMathJax(String input) {
    if (input.contains("+++\n$$")) {
      return true;
    } else {
      return false;
    }
  }

  protected File createDataDir(File file) {
    String child = file.getName().contains(".") ? file.getName().substring(0, file.getName().lastIndexOf('.')) : file.getName();
    File dataDir = new File(file.getParent(), child + DATADIR_NAME);
    log.debug("using target data dir {}", dataDir);

    if (!dataDir.exists()) {
      try {
        java.nio.file.Files.createDirectories(dataDir.toPath());
      } catch (IOException e) {
        log.error("Could not create datadir {}", dataDir, e);
        throw new RuntimeException(e);
      }
    }
    return dataDir;
  }

  private String addMathJax(String render, String mathjaxDir) {
    int index = render.lastIndexOf("</head>");
    if (index > 0) {
      String first = render.substring(0, index);
      String last = render.substring(index);

      return first + mathJaxStart + mathjaxDir + mathJaxEnd + last;
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
