package de.ks.idnadrev.information.chart.adoc;

import de.ks.flatadocdb.session.Session;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.information.ChartData;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.entity.information.ChartType;
import de.ks.idnadrev.information.chart.ChartInfoActivity;
import de.ks.idnadrev.information.chart.ChartInfoController;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class ChartFileRenderingTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(ChartFileRenderingTest.class);

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule());

  private ChartInfoController controller;
  private String chartId;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return ChartInfoActivity.class;
  }

  @Override
  protected void createTestData(Session session) {
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
    session.persist(chart);

    chartId = persistentWork.from(ChartInfo.class).get(0).getId();
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