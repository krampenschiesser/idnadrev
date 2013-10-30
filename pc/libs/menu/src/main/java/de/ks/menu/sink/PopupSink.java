package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.EventBus;
import de.ks.menu.MenuItem;
import de.ks.menu.event.MenuItemClickedEvent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 */
public class PopupSink extends AbstractSink<PopupSink> {
  private static final Logger log = LogManager.getLogger(PopupSink.class);
  private Stage mockStage = null;
  @Inject
  @MenuItem("")
  Instance<Object> menuItem;


  @Inject
  public PopupSink(EventBus bus) {
    super(bus);
  }

  @Override
  public void onEvent(MenuItemClickedEvent event) {
    if (Platform.isFxApplicationThread()) {
      showPopup(event);
    } else {
      Platform.runLater(() -> showPopup(event));
    }
  }

  private void showPopup(MenuItemClickedEvent event) {
    if (event.getItem().getMenuPath().startsWith(getMenuPath())) {
      Stage stage = createStage();
      Instance<?> select = menuItem.select(event.getTarget());
      Object menuItem = select.get();
      if (menuItem instanceof Parent) {
        stage.setScene(new Scene((Parent) menuItem));
      } else if (menuItem instanceof Node) {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add((Node) menuItem);
        stage.setScene(new Scene(stackPane));
      } else {
        log.error("Could not handle MenuItemClickedEvent because {} is neither {} nor {}", event.getTarget(), Parent.class.getName(), Node.class.getName());
      }
      stage.show();
    }
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
