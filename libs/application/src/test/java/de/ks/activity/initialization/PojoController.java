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
package de.ks.activity.initialization;

import de.ks.activity.ActivityController;
import javafx.fxml.Initializable;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

public class PojoController implements Initializable {
  @Inject
  ActivityController controller;
  private URL location;
  private ResourceBundle resources;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.location = location;
    this.resources = resources;
  }

  public ActivityController getController() {
    return controller;
  }

  public URL getLocation() {
    return location;
  }

  public ResourceBundle getResources() {
    return resources;
  }
}
