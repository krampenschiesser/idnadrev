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
package de.ks.texteditor.launch;

import com.github.rjeschke.txtmark.Processor;
import de.ks.texteditor.module.TextEditorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class MarkdownParser {
  private static final Logger log = LoggerFactory.getLogger(MarkdownParser.class);
  public static final String CSS_FILE_NAME = "markdown.css";
  private final Path dataDir;

  @Inject
  public MarkdownParser(@Named(TextEditorModule.DATA_DIR) Path dataDir) {
    this.dataDir = dataDir;
  }

  public String parse(String plainMarkDown) {
    String body = Processor.process(plainMarkDown, true);
    return createHtmlPage(body);
  }

  public String parse(File markdownFile) {
    try {
      String body = Processor.process(markdownFile, true);
      return createHtmlPage(body);
    } catch (IOException e) {
      log.error("Could not parse markdown file {}", markdownFile, e);
      return null;
    }
  }

  protected String createHtmlPage(String body) {
    return "<!DOCTYPE html>\n" +
      "<html lang=\"en\">\n" +
      "<head>\n" +
      "<meta charset=\"UTF-8\">\n" +
      "<link rel=\"stylesheet\" href=\"" + dataDir.resolve(CSS_FILE_NAME).toAbsolutePath().toString() + "\">\n" +
      "</head>\n" +
      "<body class=\"markdown-body\">\n" +
      body +
      "</body>\n" +
      "</html>";
  }
}
