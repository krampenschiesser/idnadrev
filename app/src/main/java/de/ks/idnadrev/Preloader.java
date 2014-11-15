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
package de.ks.idnadrev;

import de.ks.preload.LaunchListener;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Preloader implements Initializable, LaunchListener {
  @FXML
  private ImageView image;
  @FXML
  private Text title;
  @FXML
  private Text version;
  @FXML
  private ProgressBar loadingProgress;
  @FXML
  private StackPane root;
  private int totalWaves;
  private AtomicInteger waveCounter = new AtomicInteger();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    image.fitHeightProperty().bind(root.heightProperty());
    loadingProgress.setProgress(0.0);

    image.setOpacity(0);
    Timeline timeline = new Timeline();
    timeline.setCycleCount(1);
    KeyFrame keyFrame = new KeyFrame(Duration.seconds(6), new KeyValue(image.opacityProperty(), 1.0));
    timeline.getKeyFrames().add(keyFrame);
    timeline.play();
  }

  public Text getVersion() {
    return version;
  }

  @Override
  public void totalWaves(int count) {
    totalWaves = count;

  }

  @Override
  public void waveStarted(int prio) {
    int count = waveCounter.incrementAndGet();
    Platform.runLater(() -> {
      double progress = (double) count / (double) totalWaves;
      loadingProgress.setProgress(progress);
    });
  }

  @Override
  public void waveFinished(int prio) {
  }

  @Override
  public void failure(String msg) {
  }

  @Override
  public void wavePriorities(Set<Integer> integers) {

  }
}
