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

import java.util.LinkedList;
import java.util.List;

public class ChartPreviewData {
  public static class DataSeries {
    protected String title;
    protected List<Double> values;

    public DataSeries(String title, List<Double> values) {
      this.title = title;
      this.values = values;
    }

    public String getTitle() {
      return title;
    }

    public List<Double> getValues() {
      return values;
    }
  }

  protected final List<String> categories = new LinkedList<>();
  protected final List<DataSeries> series = new LinkedList<>();
  protected String xAxisTitle;
  protected String yAxisTitle;

  public List<String> getCategories() {
    return categories;
  }

  public List<DataSeries> getSeries() {
    return series;
  }

  public void addSeries(String title, List<Double> values) {
    this.series.add(new DataSeries(title, values));
  }

  public String getxAxisTitle() {
    return xAxisTitle;
  }

  public void setxAxisTitle(String xAxisTitle) {
    this.xAxisTitle = xAxisTitle;
  }

  public String getyAxisTitle() {
    return yAxisTitle;
  }

  public void setyAxisTitle(String yAxisTitle) {
    this.yAxisTitle = yAxisTitle;
  }
}
