package de.ks.idnadrev.information.chart.adoc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

@RunWith(LauncherRunner.class)
public class ChartFileRenderingTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(ChartFileRenderingTest.class);
  private ChartInfoController controller;
  private long chartId;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ChartInfoActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
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

    chartId = PersistentWork.from(ChartInfo.class).get(0).getId();
  }

  @Inject
  ChartFileRendering rendering;

  @Test
  public void testRenderToFile() throws Exception {
    Path tempfile = Files.createTempFile("test", ".png");
    tempfile.toFile().deleteOnExit();
    rendering.renderToFile(chartId, tempfile);

    assertTrue(tempfile.toFile().exists());
  }
}