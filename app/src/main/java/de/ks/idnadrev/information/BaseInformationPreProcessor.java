/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.information;

import de.ks.idnadrev.information.chart.adoc.ChartFileRendering;
import de.ks.idnadrev.information.uml.adoc.DiagramPreProcessor;
import de.ks.text.process.AsciiDocPreProcessor;
import de.ks.text.view.AsciiDocViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseInformationPreProcessor implements AsciiDocPreProcessor {
  private static final Logger log = LoggerFactory.getLogger(DiagramPreProcessor.class);

  protected final Pattern pattern;
  protected final String infoPrefix;
  protected final String infoPrefixFull;

  public BaseInformationPreProcessor(String infoPrefix) {
    this.infoPrefix = infoPrefix;
    this.infoPrefixFull = infoPrefix + "::";
    pattern = Pattern.compile(infoPrefixFull + "\\d*");
  }

  @Override
  public String preProcess(String adoc, AsciiDocViewer viewer) {
    if (adoc == null) {
      return null;
    }
    Map<String, Path> tasks = new HashMap<>();
    StringBuilder retval = new StringBuilder();

    Matcher matcher = pattern.matcher(adoc);
    int last = 0;
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();

      retval.append(adoc.substring(last, start));
      String idString = adoc.substring(start + infoPrefixFull.length(), end);
      String pathName = infoPrefix + idString;
      String tmpDir = System.getProperty("java.io.tmpdir");

      Path tempFilePath = Paths.get(tmpDir, pathName + "." + ChartFileRendering.IMAGE_FORMAT);
      File file = tempFilePath.toFile();
      if (!file.exists()) {
        try {
          tasks.put(idString, tempFilePath);
        } catch (NumberFormatException e) {
          log.warn("Could not parse idString {}", idString);
        }
        if (viewer != null) {
//          viewer.addSuspensionRunnable(new FileDeletionRunnable(file));
          // FIXME: 12/23/15
        }
      }


      retval.append("\n");
      retval.append("image::file:///");
      retval.append(file.getAbsolutePath());
      retval.append("[]");
      retval.append("\n");
      last = end;
    }
    if (last < adoc.length()) {
      retval.append(adoc.substring(last, adoc.length()));
    }

    handleIds(tasks);
    return retval.toString();
  }

  protected abstract void handleIds(Map<String, Path> tasks);
}
