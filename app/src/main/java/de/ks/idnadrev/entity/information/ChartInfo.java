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
package de.ks.idnadrev.entity.information;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Entity
@AssociationOverrides(@AssociationOverride(name = "tags", joinTable = @JoinTable(name = "chartinfo_tag")))
public class ChartInfo extends Information<ChartInfo> {
  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(ChartInfo.class);
  private static final ObjectMapper mapper = new ObjectMapper();

  @Enumerated(EnumType.STRING)
  protected ChartType chartType;

  @Transient
  protected transient ChartData data;

  protected ChartInfo() {
    chartType = ChartType.LINE;
  }

  public ChartInfo(String name, ChartType chartType) {
    super(name);
    this.chartType = chartType;
  }

  public ChartType getChartType() {
    return chartType;
  }

  public void setChartType(ChartType chartType) {
    this.chartType = chartType;
  }

  public ChartInfo setChartData(ChartData data) {
    try {
      this.data = data;
      if (data != null) {
        data.setChartType(chartType);
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, data);
        setContent(writer.toString());
      } else {
        setContent(null);
      }
    } catch (IOException e) {
      log.error("Could not write chartdata {}", data, e);
    }
    return this;
  }

  public ChartData getChartData() {
    if (data == null && getContent() != null) {
      try {
        data = mapper.readValue(new StringReader(getContent()), ChartData.class);
        data.setChartType(chartType);
      } catch (IOException e) {
        log.error("Could not read chartdata from {}", getContent(), e);
      }
    }
    return data;
  }
}
