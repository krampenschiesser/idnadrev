package de.ks.editor;/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.Grid2DEditorProvider;
import de.ks.i18n.Localized;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public abstract class AbstractEditor implements Grid2DEditorProvider<Label, Node> {
  private static final Logger log = LoggerFactory.getLogger(AbstractEditor.class);
  protected Label descriptor = new Label();
  protected Field field;

  public void forField(Field field) {
    this.field = field;
    descriptor.setText(Localized.get(field) + ":");
  }

  @Override
  public Label getDescriptor() {
    return descriptor;
  }
}
