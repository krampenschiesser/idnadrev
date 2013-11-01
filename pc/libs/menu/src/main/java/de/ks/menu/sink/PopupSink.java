package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.eventsystem.bus.EventBus;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;

/**
 *
 */
public class PopupSink extends AbstractSink<PopupSink> {
  private static final Logger log = LogManager.getLogger(PopupSink.class);
  private Stage mockStage = null;


  @Inject
  public PopupSink(EventBus bus) {
    super(bus);
  }

  protected void showMenuItem(Object menuItem) {
    Stage stage = createStage();
    if (menuItem instanceof Parent) {
      stage.setScene(new Scene((Parent) menuItem));
    } else if (menuItem instanceof Node) {
      StackPane stackPane = new StackPane();
      stackPane.getChildren().add((Node) menuItem);
      stage.setScene(new Scene(stackPane));
    } else {
      log.error("Could not handle MenuItemClickedEvent because {} is neither {} nor {}", menuItem.getClass(), Parent.class.getName(), Node.class.getName());
    }
    stage.show();
  }

  private Stage createStage() {
    Stage retval;
    if (mockStage != null) {
      retval = mockStage;
    } else {
      retval = new Stage(StageStyle.UTILITY);
    }
    return retval;
  }

  protected void mockWindow(Stage stage) {
    mockStage = stage;
  }
}
