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
package de.ks.idnadrev.information.chart.adoc;

import de.ks.executor.JavaFXExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.file.Path;

public class ChartFileRendering {
  private static final Logger log = LoggerFactory.getLogger(ChartFileRendering.class);
  public static final int RENDERED_WIDTH = 900;
  public static final String IMAGE_FORMAT = "png";
  @Inject
  JavaFXExecutorService fxExecutorService;

  public void renderToFile(String id, Path tempFilePath) {
//    ChartInfo chartInfo = persistentWork.byId(ChartInfo.class, id);
//    if (chartInfo != null) {
//      log.debug("Rendering chart {} to file {}", id, tempFilePath);
//
//      Future<WritableImage> future = fxExecutorService.submit(() -> {
//        ChartPreviewHelper helper = new ChartPreviewHelper(null);
//        Chart chart = helper.createNewChart(chartInfo);
//        chart.setPrefWidth(RENDERED_WIDTH);
//
//        StackPane pane = new StackPane(chart);
//        Popup popup = new Popup();
//        popup.getContent().add(pane);
//
//        return pane.snapshot(null, null);
//      });
//
//      try {
//        WritableImage image = future.get();
//
//        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
//        try {
//          ImageIO.write(bufferedImage, IMAGE_FORMAT, tempFilePath.toFile());
//        } catch (IOException e) {
//          log.error("Could not write image {}", tempFilePath, e);
//        }
//      } catch (InterruptedException e) {
//        //
//      } catch (ExecutionException e) {
//        throw new RuntimeException(e.getCause());
//      }
//    } else {
//      log.warn("No chart found for ID {}", id);
//    }
  }

}
