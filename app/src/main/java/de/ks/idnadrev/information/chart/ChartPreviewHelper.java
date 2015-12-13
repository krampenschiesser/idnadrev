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
package de.ks.idnadrev.information.chart;

import javafx.collections.FXCollections;
import javafx.scene.chart.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ChartPreviewHelper {
  protected XYChart<String, Number> xyChart;
  protected PieChart pieChart;

  protected final ChartInfoController controller;
  protected ChartInfo chartInfo;

  public ChartPreviewHelper() {
    controller = null;
  }

  public ChartPreviewHelper(ChartInfoController controller) {
    this.controller = controller;
  }

  public Chart createNewChart(ChartInfo info) {
    cleanup();

    this.chartInfo = info;

    ChartType chartType = info.getChartType();

    if (chartType == ChartType.PIE) {
      this.pieChart = new PieChart();

      if (controller != null) {
        pieChart.titleProperty().bind(controller.name.textProperty());
      } else {
        pieChart.titleProperty().set(info.getName());
      }
    } else {
      Supplier<XYChart<String, Number>> supplier = getXYChartSupplier(info);
      xyChart = supplier.get();

      if (controller != null) {
        xyChart.titleProperty().bind(controller.name.textProperty());
      } else {
        xyChart.titleProperty().set(info.getName());
      }
    }

    recompute(chartInfo.getChartData());

    if (xyChart != null) {
      return xyChart;
    } else {
      return pieChart;
    }
  }

  private void cleanup() {
    if (pieChart != null) {
      pieChart.titleProperty().unbind();
      pieChart.dataProperty().unbind();
      pieChart = null;
    }
    if (xyChart != null) {
      xyChart.getXAxis().labelProperty().unbind();
      xyChart.getYAxis().labelProperty().unbind();
      xyChart.titleProperty().unbind();
      xyChart = null;
    }
  }

  protected void recompute(ChartData data) {
    if ((xyChart == null && pieChart == null) || data.getSeries().isEmpty()) {
      return;
    }
    if (pieChart != null) {
      fillPieChart(data);
    } else if (xyChart != null) {
      fillXYChart(data);
    }
  }

  protected Supplier<XYChart<String, Number>> getXYChartSupplier(ChartInfo chartInfo) {
    ChartType chartType = chartInfo.getChartType();

    final Supplier<XYChart<String, Number>> supplier;
    final CategoryAxis categoryAxis = new CategoryAxis();
    final NumberAxis numberAxis = new NumberAxis();
    numberAxis.setAnimated(true);
    numberAxis.setAutoRanging(true);
    categoryAxis.setGapStartAndEnd(false);


    if (controller != null) {
      numberAxis.labelProperty().bind(controller.yaxisTitle.textProperty());
    } else {
      numberAxis.labelProperty().set(chartInfo.getChartData().getYAxisTitle());
    }

    if (chartType == ChartType.LINE) {
      supplier = () -> new LineChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.AREA) {
      supplier = () -> new AreaChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.BAR) {
      supplier = () -> new BarChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.BUBBLE) {
      supplier = () -> new BubbleChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.SCATTER) {
      supplier = () -> new ScatterChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.STACKEDBAR) {
      supplier = () -> new StackedBarChart<String, Number>(categoryAxis, numberAxis);
    } else if (chartType == ChartType.STACKEDAREA) {
      supplier = () -> new StackedAreaChart<String, Number>(categoryAxis, numberAxis);
    } else {
      supplier = null;
    }
    return () -> {
      XYChart<String, Number> chart = supplier.get();
      return chart;
    };
  }

  protected void fillPieChart(ChartData data) {
    ChartData.DataSeries series = data.getSeries().get(0);

    List<PieChart.Data> pieChartSeries = new LinkedList<PieChart.Data>();
    for (int i = 0; i < series.getValues().size(); i++) {
      Double value = series.getValues().get(i);
      String category = data.getCategories().get(i);
      if (category != null) {
        pieChartSeries.add(new PieChart.Data(category, value));
      }
    }
    pieChart.setData(FXCollections.observableList(pieChartSeries));
  }

  protected void fillXYChart(ChartData data) {
    List<XYChart.Series<String, Number>> allSeries = new ArrayList<>(data.getSeries().size());

    for (ChartData.DataSeries dataSeries : data.getSeries()) {
      XYChart.Series<String, Number> series = new XYChart.Series<>();
      series.setName(dataSeries.getTitle());

      List<XYChart.Data<String, Number>> datas = new LinkedList<XYChart.Data<String, Number>>();
      for (int i = 0; i < dataSeries.getValues().size(); i++) {
        Double value = dataSeries.getValues().get(i);
        String category = data.getCategories().get(i);

        datas.add(new XYChart.Data<String, Number>(category, value));
      }
      series.setData(FXCollections.observableList(datas));
      allSeries.add(series);
    }
    xyChart.setData(FXCollections.observableList(allSeries));
    xyChart.getXAxis().setLabel(data.getXAxisTitle());
  }
}
