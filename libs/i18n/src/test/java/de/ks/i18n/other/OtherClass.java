/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.i18n.other;

import de.ks.i18n.Localized;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class OtherClass {
  public String getString() {
    return Localized.get("subPackageString");
  }

  public String getStringFromFXML() throws IOException {
    StackPane loaded = FXMLLoader.load(getClass().getResource("Local.fxml"));
    Label node = (Label) loaded.getChildren().get(0);
    return node.getText();
  }
}
