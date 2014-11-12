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
package de.ks.idnadrev.information.chart.adoc;

import de.ks.activity.executor.ActivityExecutor;
import de.ks.text.preprocess.AsciiDocPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartPreProcessor implements AsciiDocPreProcessor {
  private static final Logger log = LoggerFactory.getLogger(ChartPreProcessor.class);
  public static final String KEY_CHART = "chart::";

  @Inject
  ActivityExecutor executor;
  protected final Pattern compile = Pattern.compile(KEY_CHART + "\\d*");

  @Override
  public String preProcess(String adoc) {
    StringBuilder retval = new StringBuilder();

    Matcher matcher = compile.matcher(adoc);
    int last = 0;
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();

      retval.append(adoc.substring(last, start));
      String chartPath = "chart" + adoc.substring(start + KEY_CHART.length(), end) + "_";
      try {
        Path tempFilePath = Files.createTempFile(chartPath, ".png");
        tempFilePath.toFile().deleteOnExit();
        retval.append("\n");
        retval.append("image::file://");
        retval.append(tempFilePath.toFile().getAbsolutePath());
        retval.append("\n");
      } catch (IOException e) {
        log.error("Could not create tempfile for '{}'", chartPath, e);
      }
      last = end;
    }
    return retval.toString();
  }
}
