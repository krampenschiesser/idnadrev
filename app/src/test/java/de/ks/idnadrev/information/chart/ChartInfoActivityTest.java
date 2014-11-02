package de.ks.idnadrev.information.chart;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.i18n.Localized;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.information.ChartData;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.entity.information.ChartType;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class ChartInfoActivityTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(ChartInfoActivityTest.class);
  private ChartInfoController controller;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ChartInfoActivity.class;
  }

  protected void createCategory() {
    PersistentWork.run(em -> {
      ChartData chartData = new ChartData();
      chartData.setXAxisTitle("xtitle");
      chartData.setYAxisTitle("ytitle");
      chartData.getCategories().add("cat1");
      chartData.getCategories().add("cat2");
      chartData.getCategories().add("cat3");
      chartData.addSeries("series1", Arrays.asList(1D, 3D, 5D));
      chartData.addSeries("series2", Arrays.asList(4D, 2D, 7D));
      chartData.addSeries("series3", Arrays.asList(8D, 5D, 1D));

      ChartInfo chart = new ChartInfo("chart", ChartType.BAR);
      chart.setChartData(chartData);
      em.persist(chart);
    });
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(ChartInfoController.class);
  }

  @Test
  public void testLoad() throws Exception {
    FXPlatform.waitForFX();
    createCategory();
    ChartInfo chartInfo = PersistentWork.from(ChartInfo.class).get(0);
    store.getDatasource().setLoadingHint(chartInfo);

    activityController.reload();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();


    ChartDataEditor editor = controller.editorController;
    assertEquals(3, editor.columnHeaders.size());
    for (int i = 0; i < 3; i++) {
      assertEquals("series" + (i + 1), editor.columnHeaders.get(i).getValueSafe());
    }

    assertEquals(3, editor.rows.size());

    ChartRow row = editor.rows.get(0);
    assertEquals(String.valueOf(1D), row.values.get(0).get());
    assertEquals(String.valueOf(4D), row.values.get(1).get());
    assertEquals(String.valueOf(8D), row.values.get(2).get());

    row = editor.rows.get(1);
    assertEquals(String.valueOf(3D), row.values.get(0).get());
    assertEquals(String.valueOf(2D), row.values.get(1).get());
    assertEquals(String.valueOf(5D), row.values.get(2).get());

    row = editor.rows.get(2);
    assertEquals(String.valueOf(5D), row.values.get(0).get());
    assertEquals(String.valueOf(7D), row.values.get(1).get());
    assertEquals(String.valueOf(1D), row.values.get(2).get());

    assertEquals(1, controller.previewContainer.getChildren().size());
    BarChart<String, Double> chart = (BarChart<String, Double>) controller.previewContainer.getChildren().get(0);
    assertEquals(3, chart.getData().size());
    assertEquals("chart", chart.getTitle());
    assertEquals("xtitle", chart.getXAxis().getLabel());
    assertEquals("ytitle", chart.getYAxis().getLabel());
  }

  @Test
  public void testSave() throws Exception {
    FXPlatform.waitForFX();
    assertEquals(1, controller.previewContainer.getChildren().size());
    LineChart<String, Double> chart = (LineChart<String, Double>) controller.previewContainer.getChildren().get(0);

    assertEquals(2, chart.getData().size());
    assertEquals(Localized.get("col", 1), chart.getData().get(0).getName());
    assertEquals(Localized.get("col", 2), chart.getData().get(1).getName());

    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      ChartRow row = controller.editorController.rows.get(0);
      row.setValue(0, 4D);
      row.setValue(1, 2D);
      row.setCategory("cat1");
      controller.chartType.getSelectionModel().select(ChartType.PIE);
    });
    activityController.save();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    List<ChartInfo> charts = PersistentWork.from(ChartInfo.class);
    assertEquals(1, charts.size());
    ChartInfo chartInfo = charts.get(0);
    assertEquals("test", chartInfo.getName());
    assertEquals(ChartType.PIE, chartInfo.getChartType());

    ChartData data = chartInfo.getChartData();
    assertNotNull(data);
    assertEquals(1, data.getCategories().size());
    assertEquals("cat1", data.getCategories().get(0));

    assertEquals(4, data.getSeries().get(0).getValues().get(0).intValue());
    assertEquals(2, data.getSeries().get(1).getValues().get(0).intValue());
  }
}