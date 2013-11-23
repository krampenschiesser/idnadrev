package de.ks.editor;

/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.JFXCDIRunner;
import de.ks.eventsystem.bus.EventBus;
import de.ks.workflow.cdi.WorkflowContext;
import de.ks.workflow.validation.event.ValidationRequiredEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JFXCDIRunner.class)
public class StringEditorTest {
  private static final Logger log = LogManager.getLogger(StringEditorTest.class);
  @Inject
  @EditorFor(String.class)
  StringEditor editor;
  @Inject
  EventBus bus;

  private ValidationRequiredEvent event;

  @BeforeClass
  public static void beforeClass() {
//    WorkflowContext.start("test123", new SimpleWorkflowModel());
  }

  @AfterClass
  public static void afterClass() {
    WorkflowContext.stopAll();
  }

  @Before
  public void setUp() throws Exception {
//    editor.initialize(SimpleWorkflowModel.class).setName(null);
    bus.register(this);
  }

  @Test
  public void testWhatever() throws Exception {
    TextField textField = editor.getNode();
    Label descriptor = editor.getDescriptor();

//    assertEquals(Localized.get(editor.getPath().toLocalizationPath()) + ":", descriptor.getText());
    textField.setText("Hello Sauerland!");
    editor.postValidationRequiredEvent();
    assertNotNull(event);
    assertEquals("Hello Sauerland!", event.getValue());
  }

  @Subscribe
  public void onInputChange(ValidationRequiredEvent event) {
    log.info("Received {}", event);
    this.event = event;
  }
}
