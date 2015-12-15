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

import de.ks.idnadrev.information.BaseInformationPreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Map;

public class ChartPreProcessor extends BaseInformationPreProcessor {
  private static final Logger log = LoggerFactory.getLogger(ChartPreProcessor.class);

  @Inject
  ChartFileRendering fileRendering;

  public ChartPreProcessor() {
    super("chart");
  }

  @Override
  protected void handleIds(Map<Long, Path> tasks) {
    tasks.forEach((id, path) -> fileRendering.renderToFile(id, path));
  }
}
