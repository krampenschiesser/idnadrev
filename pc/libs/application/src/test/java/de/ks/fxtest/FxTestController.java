package de.ks.fxtest;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

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
