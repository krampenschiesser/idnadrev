package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.application.fxml.DefaultLoader;
import de.ks.editor.AbstractEditor;
import de.ks.editor.Detailed;
import de.ks.editor.DetailedLiteral;
import de.ks.editor.EditorForLiteral;
import de.ks.eventsystem.bus.EventBus;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.i18n.Localized;
import de.ks.persistence.entity.AbstractPersistentObject;
import de.ks.reflection.ReflectionUtil;
import de.ks.workflow.WorkflowState;
import de.ks.workflow.cdi.DefaultLiteral;
import de.ks.workflow.cdi.WorkflowSpecificLiteral;
import de.ks.workflow.validation.event.ValidationResultEvent;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 *
 */
public class EditStep extends InteractiveStep<GridPane> {
  @Inject
  WorkflowState workflowState;
  @Inject
  @Any
  Instance<AbstractEditor> editorProvider;
  @Inject
  EventBus eventBus;

  private EditStepGrid controller;
  private Map<String, AbstractEditor> registeredEditors = new HashMap<>();

  @PostConstruct
  public void initialize() {
    DefaultLoader<StackPane, EditStepGrid> loader = new DefaultLoader<>(EditStep.class.getResource("EditStepGrid.fxml"));
    Class<?> modelClass = workflowState.getModelClass();
    List<Field> fields = getFieldsToConfigure(modelClass);
    LinkedHashMap<Field, AbstractEditor> editors = getEditors(fields);
    controller = loader.getController();
    controller.getInstruction().setText(Localized.get(getTitle()));
    populateEditGrid(controller.getEditGrid(), editors);
    eventBus.register(this);
  }

  protected void populateEditGrid(GridPane editGrid, LinkedHashMap<Field, AbstractEditor> editors) {
    int row = 1;
    for (Map.Entry<Field, AbstractEditor> entry : editors.entrySet()) {
      RowConstraints rowConstraints = new RowConstraints();
      rowConstraints.setValignment(VPos.TOP);
      rowConstraints.setVgrow(Priority.NEVER);
      rowConstraints.setMinHeight(25F);
      editGrid.getRowConstraints().add(rowConstraints);

      AbstractEditor editor = entry.getValue();
      Field field = entry.getKey();
      editor.forField(field);

      editGrid.add(editor.getDescriptor(), 0, row);
      editGrid.add(editor.getNode(), 1, row);
      registeredEditors.put(field.getName(), editor);
      row++;
    }
  }

  protected LinkedHashMap<Field, AbstractEditor> getEditors(List<Field> fields) {
    LinkedHashMap<Field, AbstractEditor> retval = new LinkedHashMap<>();
    for (Field field : fields) {
      AbstractEditor editor = getEditor(field);
      retval.put(field, editor);
    }
    return retval;
  }

  protected AbstractEditor getEditor(Field field) {
    Class<?> type = field.getType();
    EditorForLiteral editorLiteral = new EditorForLiteral(type);
    List<AnnotationLiteral<?>> qualifiers = new ArrayList<>(3);
    List<AnnotationLiteral<?>> qualifierFallback = new ArrayList<>(3);
    if (field.isAnnotationPresent(Detailed.class)) {
      qualifiers.addAll(Arrays.asList(editorLiteral, new DetailedLiteral(), new WorkflowSpecificLiteral(workflowState.getWorkflowClass())));
      qualifierFallback.addAll(Arrays.asList(editorLiteral, new DetailedLiteral()));
    } else {
      qualifiers.addAll(Arrays.asList(editorLiteral, new WorkflowSpecificLiteral(workflowState.getWorkflowClass())));
      qualifierFallback.addAll(Arrays.asList(editorLiteral, new DefaultLiteral()));
    }

    AbstractEditor editor = getEditorByQualifiers(qualifiers);
    if (editor != null) {
      return editor;
    }
    editor = getEditorByQualifiers(qualifierFallback);
    if (editor != null) {
      return editor;
    }

    throw new IllegalStateException("Could not find any @Default editor for type " + type + " of field " + field);
  }

  protected AbstractEditor getEditorByQualifiers(List<AnnotationLiteral<?>> qualifiers) {
    Instance<AbstractEditor> editorInstance = editorProvider.select(AbstractEditor.class, qualifiers.toArray(new Annotation[qualifiers.size()]));
    if (editorInstance.isUnsatisfied()) {
      return null;
    } else {
      return editorInstance.get();
    }
  }

  protected List<Field> getFieldsToConfigure(Class<?> modelClass) {
    List<Field> allFields = ReflectionUtil.getAllFields(modelClass, //
            (f) -> !AbstractPersistentObject.class.equals(f.getDeclaringClass()), (f) -> !Modifier.isStatic(f.getModifiers()));
    allFields.removeAll(getIgnoredFields());
    return allFields;
  }

  protected Set<Field> getIgnoredFields() {
    return Collections.emptySet();
  }

  @Override
  public GridPane getNode() {
    return controller.getEditGrid();
  }

  public Map<String, AbstractEditor> getRegisteredEditors() {
    return registeredEditors;
  }

  public EditStepGrid getController() {
    return controller;
  }

  private static final Logger log = LogManager.getLogger(EditStep.class);

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void onValidationEvent(ValidationResultEvent event) {
    if (!event.isSuccessful()) {
      log.info("Validation failed: {}",event.getViolations());
      Node node = getEditor(event.getValidatedField()).getNode();

//      for (AbstractEditor editor : getRegisteredEditors().values()) {
//        editor.getNode().setDisable(true);
//      }
//      node.setDisable(false);
      node.requestFocus();
      TextField field = (TextField) node;
      log.info("Requesting focus! Field: {}, Text:{}", event.getValidatedField().getName(), field.getText());
    } else {
      log.info("Enabling all fields again.");
      for (AbstractEditor editor : getRegisteredEditors().values()) {
        editor.getNode().setDisable(false);
      }
    }
  }
}
