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

package de.ks.idnadrev.information.chart;

import de.ks.datasource.CreateEditDS;
import de.ks.idnadrev.entity.information.ChartInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class ChartInfoDS extends CreateEditDS<ChartInfo> {
  private static final Logger log = LoggerFactory.getLogger(ChartInfoDS.class);

  public ChartInfoDS() {
    super(ChartInfo.class);
  }

  @Override
  protected void furtherSave(EntityManager em, ChartInfo reloaded) {
    log.info("data: {}", reloaded.getChartData());
  }
}
