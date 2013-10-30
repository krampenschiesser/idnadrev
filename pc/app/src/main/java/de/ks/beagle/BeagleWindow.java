package de.ks.beagle;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.MainWindow;
import de.ks.menu.presenter.MenuBarPresenter;
import de.ks.menu.sink.PopupSink;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 *
 */
public class BeagleWindow extends MainWindow {
  private static final Logger log = LogManager.getLogger(BeagleWindow.class);

  @Inject
  MenuBarPresenter menuBarPresenter;
  @Inject
  PopupSink sink;

  private BorderPane borderPane;

  @PostConstruct
  public void initialize() {
    sink.setMenuPath("/main");
  }

  @Override
  public Parent getRoot() {
    borderPane = new BorderPane();
    borderPane.setPrefSize(640, 480);
    borderPane.setTop(menuBarPresenter.getMenu("/main"));
    return borderPane;
  }

  @Override
  public String getApplicationTitle() {
    return "Beagle Version 0.3";
  }


//  @Subscribe
//  public void onMenuItemClicked(MenuItemClickedEvent event) {
//    log.debug("Received event {}",event);
//    Instance<?> select = CDI.current().select(event.getTarget(),new MenuItemLiteral());
//    if (select.isUnsatisfied()) {
//      log.error("No bean satisfying {}", event.getTarget().getName());
//    } else if (select.isAmbiguous()) {
//      log.error("Ambigous bean {}", event.getTarget().getName());
//    } else {
//      Object object = select.get();
//      if (object instanceof Node) {
//        borderPane.setCenter((Node) object);
//      }
//    }
//  }
}
