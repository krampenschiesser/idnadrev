package de.ks.workflow.step;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.fxml.DefaultLoader;
import de.ks.editor.AbstractEditor;
import de.ks.editor.Detailed;
import de.ks.editor.DetailedLiteral;
import de.ks.editor.EditorForLiteral;
import de.ks.i18n.Localized;
import de.ks.reflection.ReflectionUtil;
import de.ks.workflow.WorkflowState;
import de.ks.workflow.cdi.DefaultLiteral;
import de.ks.workflow.cdi.WorkflowSpecificLiteral;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

  private GridPane editGrid;

  @PostConstruct
  public void initialize() {
    DefaultLoader<StackPane, Object> loader = new DefaultLoader<>(EditStep.class.getResource("EditStepGrid.fxml"));
    Class<?> modelClass = workflowState.getModelClass();
    List<Field> fields = getFieldsToConfigure(modelClass);
    LinkedHashMap<Field, AbstractEditor> editors = getEditors(fields);
    editGrid = (GridPane) loader.getView().lookup("#editGrid");
    populateEditGrid(editGrid, editors);
  }

  protected void populateEditGrid(GridPane editGrid, LinkedHashMap<Field, AbstractEditor> editors) {
    editGrid.add(new Label(Localized.get(getTitle())), 0, 0);
    int row = 1;
    for (Map.Entry<Field, AbstractEditor> entry : editors.entrySet()) {
      AbstractEditor editor = entry.getValue();
      editor.forField(entry.getKey());

      editGrid.add(editor.getDescriptor(), row, 0);
      editGrid.add(editor.getNode(), row, 1);
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

    throw new IllegalStateException("Could not find any @Default editor for type " + type);
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
    List<Field> allFields = ReflectionUtil.getAllFields(modelClass);
    allFields.removeAll(getIgnoredFields());
    return allFields;
  }

  protected Set<Field> getIgnoredFields() {
    return Collections.emptySet();
  }

  @Override
  public GridPane getNode() {
    return editGrid;
  }
}
