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
package de.ks;

import de.ks.activity.initialization.ActivityCallback;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class Dummy implements Initializable, ActivityCallback {
  public static boolean fail = false;
  private boolean resumed;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    if (fail) {
      throw new RuntimeException("Failing as requested");
    }
  }

  @Override
  public void onResume() {
    this.resumed = true;
  }

  public boolean isResumed() {
    return resumed;
  }
}
