/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.fxtest;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 */
public class FxTestController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(FxTestController.class);
  @FXML
  private Label hello;
  @FXML
  private WebView webView;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    log.info("Initialize");
  }

  @PostConstruct
  public void construct() {
    log.info("PostConstruct");
  }
}
