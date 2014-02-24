package de.ks.activity;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.application.Navigator;
import de.ks.application.PresentationArea;
import de.ks.application.fxml.DefaultLoader;
import de.ks.activity.callback.InitializeViewLinks;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class ActivityTest {
  private Navigator navigator;
  private ActivityController activityController;
  private Activity activity;

  @Before
  public void setUp() throws Exception {
    navigator = Navigator.registerNavigatorWithBorderPane(JFXCDIRunner.getStage());
    activityController = new ActivityController();

    activity = new Activity(ActivityHome.class, activityController, navigator);
    activity.withLink(ActivityHome.class, "showDetails", Navigator.RIGHT_AREA, DetailController.class);
    activity.withLink(ActivityHome.class, "switchView", OtherController.class);
    activity.withLink(OtherController.class, "back", ActivityHome.class);
  }

  @Test
  public void testViewLinkNavigation() throws Exception {
    activityController.start(activity);
    PresentationArea mainArea = navigator.getMainArea();
    Node currentNode = mainArea.getCurrentNode();
    assertNotNull(currentNode);
    Button detailButton = (Button) currentNode.lookup("#showDetails");
    assertNotNull(detailButton);
    Button switchViewButton = (Button) currentNode.lookup("#switchView");
    assertNotNull(switchViewButton);

    assertNull(navigator.getPresentationArea(Navigator.RIGHT_AREA).getCurrentNode());

    assertNotNull(detailButton.getOnAction());
    detailButton.getOnAction().handle(new ActionEvent());
    assertNotNull(navigator.getPresentationArea(Navigator.RIGHT_AREA).getCurrentNode());

    assertNotNull(switchViewButton.getOnAction());
    switchViewButton.getOnAction().handle(new ActionEvent());
    Node nextNode = navigator.getMainArea().getCurrentNode();
    assertNotNull(nextNode);

    assertNotSame(currentNode, nextNode);
    Button back = (Button) nextNode.lookup("#back");
    assertNotNull(back);

    assertNotNull(back.getOnAction());
    back.getOnAction().handle(new ActionEvent());

    assertSame(currentNode,navigator.getMainArea().getCurrentNode());
  }

  @Test
  public void testInitializeViewLinks() throws Exception {
    DefaultLoader<StackPane, ActivityHome> loader = new DefaultLoader<>(ActivityHome.class);
    InitializeViewLinks viewLinks = new InitializeViewLinks(activity.getViewLinks(), activityController);
    viewLinks.accept(loader.getController(), loader.getView());

    Button button = (Button) loader.getView().lookup("#showDetails");
    assertNotNull(button.getOnAction());

    button = (Button) loader.getView().lookup("#switchView");
    assertNotNull(button.getOnAction());
  }
}
