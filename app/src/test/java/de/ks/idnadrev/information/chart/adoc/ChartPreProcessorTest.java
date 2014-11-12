package de.ks.idnadrev.information.chart.adoc;

import com.google.common.io.Files;
import de.ks.activity.executor.ActivityExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ChartPreProcessorTest {
  private static final Logger log = LoggerFactory.getLogger(ChartPreProcessorTest.class);
  private ChartPreProcessor preProcessor;
  private File tempDir;

  @Before
  public void setUp() throws Exception {
    preProcessor = new ChartPreProcessor();
    preProcessor.executor = new ActivityExecutor("test", 2, 2);

    tempDir = Files.createTempDir();
  }

  @After
  public void tearDown() throws Exception {
    preProcessor.executor.shutdown();
  }

  @Test
  public void testReplacing() throws Exception {
    String text = "chart::103\n\n== Title\nbla blubb\nchart::1 ende\nchart::1";

    String result = preProcessor.preProcess(text);
    log.info(result);
    assertThat(result, containsString("image::"));

  }
}