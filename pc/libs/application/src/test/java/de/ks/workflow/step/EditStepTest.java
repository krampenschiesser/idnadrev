package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.JFXCDIRunner;
import de.ks.editor.AbstractEditor;
import de.ks.editor.EditorFor;
import de.ks.editor.StringEditor;
import de.ks.eventsystem.EventSystem;
import de.ks.workflow.TestWorkflow;
import de.ks.workflow.navigation.WorkflowNavigator;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.validation.event.ValidationResultEvent;
import de.ks.workflow.view.full.FullWorkflowView;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
import java.util.Map;

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
  private ValidationResultEvent lastValidation;


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
    assertEquals(6, children.size());
    assertLabel(children.get(0));
    assertTextArea(children.get(1));
    assertLabel(children.get(2));
    assertHtmlEditor(children.get(3));
    assertLabel(children.get(4));
    assertTextField(children.get(5));
  }

  protected void assertLabel(Node node) {
    assertTrue("Expected Label, but was " + node.getClass().getSimpleName(), node instanceof Label);
  }

  protected void assertTextField(Node node) {
    assertTrue("Expected TextField, but was " + node.getClass().getSimpleName(), node instanceof TextField);
  }

  protected void assertTextArea(Node node) {
    assertTrue("Expected TextArea, but was " + node.getClass().getSimpleName(), node instanceof TextArea);
  }

  protected void assertHtmlEditor(Node node) {
    assertTrue("Expected HtmlEditor, but was " + node.getClass().getSimpleName(), node instanceof HTMLEditor);
  }

  @Test
  public void testValidation() throws Exception {
    EventSystem.bus.register(this);
    EventSystem.setWaitForEvents(true);

    StackPane content = view.getContent();
    EditStep editStep = navigator.getCurrentStep().getStep();

    Map<String, AbstractEditor> registeredEditors = editStep.getRegisteredEditors();
    StringEditor nameEditor = (StringEditor) registeredEditors.get("name");
    assertNotNull(nameEditor);

    expectNoValidationError();
    nameEditor.onFocusChange(false, true);
    expectValidationError("name");

    nameEditor.onFocusChange(true, false);
    expectValidationError("name");

    TextArea validationMessage = editStep.getController().getValidationMessage();
    assertTrue(validationMessage.isVisible());
    assertFalse(validationMessage.getText().isEmpty());
    assertTrue(validationMessage.getText().contains("not"));
    assertTrue(validationMessage.getText().contains("empty"));
  }

  private void expectValidationError(String name) {
    assertNotNull("No validation found ", lastValidation);
    assertFalse(lastValidation.isSuccessful());

    assertEquals(name, lastValidation.getValidatedField().getName());
    lastValidation = null;
  }

  private void expectNoValidationError() {
    assertNull(lastValidation);

  }

  @Subscribe
  public void onValidationResult(ValidationResultEvent event) {
    this.lastValidation = event;
  }
}
