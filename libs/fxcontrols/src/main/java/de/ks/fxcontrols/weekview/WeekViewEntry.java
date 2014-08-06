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

package de.ks.fxcontrols.weekview;

import javafx.scene.control.Button;
import javafx.scene.control.Control;

import java.time.Duration;
import java.time.LocalDateTime;

public class WeekViewEntry {
  protected String title;

  protected LocalDateTime start;
  protected Duration duration;
  protected Button node;

  public WeekViewEntry(String title, LocalDateTime start, Duration duration) {
    this.title = title;
    this.start = start;
    this.duration = duration;
  }

  public Control getControl() {
    if (node == null) {
      Button button = new Button(title);
      button.setPrefWidth(Control.USE_COMPUTED_SIZE);
      button.setMaxWidth(Double.MAX_VALUE);
      node = button;
    }
    return node;
  }

  public String getTitle() {
    return title;
  }

  public LocalDateTime getStart() {
    return start;
  }

  public Duration getDuration() {
    return duration;
  }
}
