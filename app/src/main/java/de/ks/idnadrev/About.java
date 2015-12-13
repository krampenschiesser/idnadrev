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

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@MenuItem("/main/help")
public class About implements NodeProvider<StackPane> {
  private static final Logger log = LoggerFactory.getLogger(About.class);

  @Override
  public StackPane getNode() {
    String javaVersion = System.getProperty("java.version");
    StackPane view = new DefaultLoader<StackPane, Object>(getClass().getResource("about.fxml")).getView();
    Label versionLabel = (Label) view.lookup("#idnadrevVersion");
    versionLabel.setText(Application.versioning.getVersionInfo().getVersionString());
    Label lookup = (Label) view.lookup("#javaVersion");
    lookup.setText(javaVersion);
    return view;
  }
}
