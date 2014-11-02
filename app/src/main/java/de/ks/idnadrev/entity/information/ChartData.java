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
package de.ks.idnadrev.entity.information;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartData implements Serializable {
  public static class DataSeries implements Serializable {
    protected String title;
    protected List<Double> values;

    protected DataSeries() {
      //json
    }

    public DataSeries(String title, List<Double> values) {
      this.title = title;
      this.values = new ArrayList<>(values);//serializeability
    }

    public String getTitle() {
      return title;
    }

    public List<Double> getValues() {
      return values;
    }
  }

  protected final List<String> categories = new ArrayList<>();
  protected final List<DataSeries> series = new ArrayList<>();
  protected String xAxisTitle;
  protected String yAxisTitle;
  protected ChartType chartType;

  public ChartData() {
    //
  }

  public List<String> getCategories() {
    return categories;
  }

  public List<DataSeries> getSeries() {
    return series;
  }

  public ChartData addSeries(String title, List<Double> values) {
    this.series.add(new DataSeries(title, values));
    return this;
  }

  public String getXAxisTitle() {
    return xAxisTitle;
  }

  public ChartData setXAxisTitle(String xAxisTitle) {
    this.xAxisTitle = xAxisTitle;
    return this;
  }

  public String getYAxisTitle() {
    return yAxisTitle;
  }

  public ChartData setYAxisTitle(String yAxisTitle) {
    this.yAxisTitle = yAxisTitle;
    return this;
  }

  public ChartType getChartType() {
    return chartType;
  }

  public void setChartType(ChartType chartType) {
    this.chartType = chartType;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ChartData{");
    sb.append("xAxisTitle='").append(xAxisTitle).append('\'');
    sb.append(", yAxisTitle='").append(yAxisTitle).append('\'');
    sb.append(", ").append(categories.size()).append(" categories");
    for (int i = 0; i < series.size(); i++) {
      sb.append(", series #'").append(i).append(" title=").append(series.get(i).getTitle());
    }
    sb.append('}');
    return sb.toString();
  }
}
