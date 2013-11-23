package de.ks.workflow;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.step.DefaultOutput;
import de.ks.workflow.step.WorkflowStepConfig;
import de.ks.workflow.view.full.FullWorkflowView;
import de.ks.workflow.view.navigator.ListBasedStepView;
import de.ks.workflow.view.step.HorizontalNextPreviousView;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.MultipleSelectionModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public class WorkflowNavigatorTest {
  @Inject
  private TestWorkflow workflow;
  @Inject
  WorkflowNavigator navigator;


  @After
  public void tearDown() throws Exception {
    WorkflowContext.stopAll();
  }

  @Before
  public void setUp() throws Exception {
    WorkflowContext.start(TestWorkflow.class);
    workflow = CDI.current().select(TestWorkflow.class).get();
  }

  @Test
  public void testNavigationList() throws Exception {
    FullWorkflowView fullView = workflow.getController();

    ListBasedStepView stepViewController = fullView.getStepViewController();
    assertNotNull(stepViewController);

    MultipleSelectionModel selectionModel = stepViewController.getStepList().getSelectionModel();
    ObservableList selectedItems = selectionModel.getSelectedItems();

    assertEquals(0, selectedItems.size());

    navigator.start();
    assertEquals(1, selectedItems.size());
    assertEquals(0, selectionModel.getSelectedIndex());

    navigator.next(DefaultOutput.NEXT.name());
    assertEquals(1, selectedItems.size());
    assertEquals(1, selectionModel.getSelectedIndex());
  }

  @Test
  public void testStepping() throws Exception {
    FullWorkflowView fullView = workflow.getController();

    navigator.start();

    HorizontalNextPreviousView ctrl = fullView.getStepButtonController();
    ctrl.getNext().getOnAction().handle(new ActionEvent());

    WorkflowStepConfig cfg = navigator.currentStep.get();

    MultipleSelectionModel selectionModel = fullView.getStepViewController().getStepList().getSelectionModel();
    ObservableList selectedItems = selectionModel.getSelectedItems();

    assertEquals(1, selectedItems.size());
    assertEquals(1, selectionModel.getSelectedIndex());
  }
}
