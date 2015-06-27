/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.markdown;

import com.github.rjeschke.txtmark.Processor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class MarkdownParser {
  private static final Logger log = LoggerFactory.getLogger(MarkdownParser.class);
  public static final String CSS_FILE_NAME = "markdown.css";
  protected String cssUrl;

  public String parse(String plainMarkDown) {
    String body = Processor.process(plainMarkDown, true);
    return createHtmlPage(body);
  }

  public String parse(File markdownFile) {
    try {
      String body = Processor.process(markdownFile, true);

      File directory = markdownFile.getParentFile();
      URL url = directory.toURI().toURL();

      body = StringUtils.replace(body, "src=\"", "src=\"" + url.toExternalForm());
      return createHtmlPage(body);
    } catch (IOException e) {
      log.error("Could not parse markdown file {}", markdownFile, e);
      return null;
    }
  }

  protected String createHtmlPage(String body) {
    String cssUrl = getCssUrl();
    return "<!DOCTYPE html>\n" +
      "<html lang=\"en\">\n" +
      "<head>\n" +
      "<meta charset=\"UTF-8\">\n" +
      (cssUrl != null ? "<link rel=\"stylesheet\" href=\"file:/home/scar/idnadrev/data//" + CSS_FILE_NAME + "\">\n" : "") +
      "</head>\n" +
      "<body class=\"markdown-body\">\n" +
      body +
      "</body>\n" +
      "</html>";
  }

  protected File disocverDataDir() {
    File workingDirectory;
    String pathname = "data" + File.separator + CSS_FILE_NAME;
    for (workingDirectory = new File(System.getProperty("user.dir")); workingDirectory != null && !new File(workingDirectory, pathname).exists(); workingDirectory = workingDirectory.getParentFile()) {
    }
    File dir = new File(workingDirectory, "data");
    log.info("Discovered data dir {}", dir);
    return dir;
  }

  public String getCssUrl() {
    if (cssUrl == null) {
      File file = disocverDataDir();
      File cssFile = new File(file, CSS_FILE_NAME);
      try {
        cssUrl = cssFile.toURI().toURL().toExternalForm();
      } catch (MalformedURLException e) {
        log.error("Could not resolve css file {}", cssFile, e);
      }
    }
    return cssUrl;
  }

}
