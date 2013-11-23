package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.editor.EditorFor;
import de.ks.editor.StringEditor;
import de.ks.workflow.TestWorkflow;
import de.ks.workflow.WorkflowNavigator;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class EditStepTest {
  @Inject
  TestWorkflow workflow;
  @Inject
  WorkflowNavigator navigator;
  @Inject
  @EditorFor(String.class)
  StringEditor editor;
  //------------
  FullWorkflowView view;


  @After
  public void tearDown() throws Exception {
    WorkflowContext.stopAll();
  }

  @Before
  public void setUp() throws Exception {
    WorkflowContext.start(TestWorkflow.class);
    workflow = CDI.current().select(TestWorkflow.class).get();
    navigator.start();
    view = workflow.getController();
  }

  @Test
  public void testGridConstruction() throws Exception {
    StackPane content = view.getContent();
    assertNotNull(content);
    assertEquals(1, content.getChildren().size());
    GridPane gridPane = (GridPane) content.getChildren().get(0);
    ObservableList<Node> children = gridPane.getChildren();
    assertEquals(5, children.size());
    assertLabel(children.get(0));
    assertLabel(children.get(1));
    assertHtmlEditor(children.get(2));
    assertLabel(children.get(3));
    assertTextField(children.get(4));
  }

  protected void assertLabel(Node node) {
    assertTrue("Expected Label, but was " + node.getClass().getSimpleName(), node instanceof Label);
  }

  protected void assertTextField(Node node) {
    assertTrue("Expected TextField, but was " + node.getClass().getSimpleName(), node instanceof TextField);
  }

  protected void assertHtmlEditor(Node node) {
    assertTrue("Expected HtmlEditor, but was " + node.getClass().getSimpleName(), node instanceof HTMLEditor);
  }
}