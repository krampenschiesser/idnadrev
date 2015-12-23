package de.ks.idnadrev.information.chart.adoc;

import com.google.common.io.Files;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ChartPreProcessorTest {
  private static final Logger log = LoggerFactory.getLogger(ChartPreProcessorTest.class);
  private ChartPreProcessor preProcessor;
  private File tempDir;

  @Before
  public void setUp() throws Exception {
    preProcessor = new ChartPreProcessor();
    ChartFileRendering rendering = Mockito.mock(ChartFileRendering.class);
    preProcessor.fileRendering = rendering;

    tempDir = Files.createTempDir();
  }

  @Test
  public void testReplacing() throws Exception {
    String text = "chart::103\n\n== Title\nbla blubb\nchart::1 ende\nchart::1";

    String result = preProcessor.preProcess(text, null);
    log.info(result);
    assertThat(result, containsString("image::"));

    Mockito.verify(preProcessor.fileRendering).renderToFile(Mockito.eq("103L"), Mockito.any(Path.class));
    Mockito.verify(preProcessor.fileRendering, Mockito.atMost(1)).renderToFile(Mockito.eq("1L"), Mockito.any(Path.class));
  }

  @Test
  public void testEnding() throws Exception {
    String text = "chart::103\n\n== Title\nbla blubb\nchart::1 ende\nchart::1 Sauerland";

    String result = preProcessor.preProcess(text, null);
    assertThat(result, Matchers.endsWith("Sauerland"));
  }
}