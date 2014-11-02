package de.ks.idnadrev.information.chart;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.information.ChartData;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.entity.information.ChartType;
import de.ks.persistence.PersistentWork;
import javafx.scene.chart.BarChart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class ChartInfoActivityTest extends ActivityTest {

  private ChartInfoController controller;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ChartInfoActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    ChartData chartData = new ChartData();
    chartData.setYAxisTitle("xtitle");
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
  }

  @Before
  public void setUp() throws Exception {
    controller = activityController.getControllerInstance(ChartInfoController.class);

  }

  @Test
  public void testLoad() throws Exception {
    ChartInfo chartInfo = PersistentWork.from(ChartInfo.class).get(0);
    store.getDatasource().setLoadingHint(chartInfo);

    activityController.reload();
    activityController.waitForDataSource();


    ChartDataEditor editor = controller.editorController;
    assertEquals(3, editor.columnHeaders.size());
    for (int i = 0; i < 3; i++) {
      assertEquals("series" + (i + 1), editor.columnHeaders.get(i).getValueSafe());
    }

    assertEquals(4, editor.rows.size());

    ChartRow row = editor.rows.get(1);
    assertEquals(String.valueOf(1D), row.values.get(0).get());
    assertEquals(String.valueOf(4D), row.values.get(1).get());
    assertEquals(String.valueOf(8D), row.values.get(2).get());

    row = editor.rows.get(2);
    assertEquals(String.valueOf(3D), row.values.get(0).get());
    assertEquals(String.valueOf(2D), row.values.get(1).get());
    assertEquals(String.valueOf(5D), row.values.get(2).get());

    row = editor.rows.get(3);
    assertEquals(String.valueOf(5D), row.values.get(0).get());
    assertEquals(String.valueOf(7D), row.values.get(1).get());
    assertEquals(String.valueOf(1D), row.values.get(2).get());

    assertEquals(1, controller.previewContainer.getChildren().size());
    BarChart<String, Double> chart = (BarChart<String, Double>) controller.previewContainer.getChildren().get(0);
    assertEquals(3, chart.getData().size());
  }
}