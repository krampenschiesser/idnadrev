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
package de.ks.idnadrev.information.view.preview;

import de.ks.BaseController;
import de.ks.activity.ActivityHint;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.idnadrev.information.chart.ChartInfoActivity;
import de.ks.idnadrev.information.chart.ChartPreviewHelper;
import de.ks.idnadrev.information.view.InformationPreviewItem;
import de.ks.persistence.PersistentWork;
import javafx.fxml.FXML;
import javafx.scene.chart.Chart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ChartPreview extends BaseController<List<InformationPreviewItem>> implements InformationPreview<ChartInfo> {
  protected volatile InformationPreviewItem selectedItem;
  @FXML
  protected StackPane chartContainer;

  protected final Map<String, ChartInfo> infos = new ConcurrentHashMap<>();
  protected final ChartPreviewHelper previewHelper = new ChartPreviewHelper();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  @Override
  public Pane show(InformationPreviewItem item) {
    selectedItem = item;
    if (infos.containsKey(item.getName())) {
      Chart chart = previewHelper.createNewChart(infos.get(item.getName()));
      chart.getStyleClass().add("fullScreenBg");
      chartContainer.getChildren().clear();
      chartContainer.getChildren().add(chart);
    }
    return chartContainer;
  }

  @Override
  public void edit() {
    ActivityHint activityHint = new ActivityHint(ChartInfoActivity.class, controller.getCurrentActivityId());
    String name = selectedItem.getName();
    ChartInfo diagramInfo = infos.get(name);

    activityHint.setDataSourceHint(() -> diagramInfo);
    controller.startOrResume(activityHint);
  }


  @Override
  protected void onRefresh(List<InformationPreviewItem> model) {
    infos.clear();
    model.stream()//
      .filter(preview -> preview.getType().equals(ChartInfo.class))//
      .forEach(this::load);
  }

  private void load(InformationPreviewItem informationPreviewItem) {
    CompletableFuture.supplyAsync(() -> {
      ChartInfo chartInfo = (ChartInfo) PersistentWork.forName(informationPreviewItem.getType(), informationPreviewItem.getName());
      chartInfo.getChartData();//deserialize
      infos.put(chartInfo.getName(), chartInfo);
      return chartInfo;
    }, controller.getExecutorService())//
      .thenAcceptAsync(info -> {
        if (informationPreviewItem.equals(selectedItem)) {
          Chart chart = previewHelper.createNewChart(info);
          chartContainer.getChildren().clear();
          chartContainer.getChildren().add(chart);
        }
      }, controller.getJavaFXExecutor());
  }

  @Override
  public ChartInfo getCurrentItem() {
    String name = selectedItem.getName();
    return infos.get(name);
  }
}
