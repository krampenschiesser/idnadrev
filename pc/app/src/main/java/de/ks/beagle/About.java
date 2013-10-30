package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import de.ks.menu.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 *
 */
@MenuItem("/main/help")
public class About extends StackPane {
  private static final Logger log = LogManager.getLogger(About.class);

  public About() {
    WebView webView = new WebView();
    URL resource = getClass().getResource("about.html");
    try {
      StringBuilder builder = new StringBuilder();
      List<String> lines = Files.readLines(new File(resource.getFile()), Charsets.UTF_8);
      for (String line : lines) {
        builder.append(line);
      }
      webView.getEngine().loadContent(builder.toString());
    } catch (IOException e) {
      log.error("Could not load about.html", e);
    }
    getChildren().add(webView);
  }
}
