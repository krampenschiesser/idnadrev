package de.ks.editor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.util.AnnotationLiteral;

/**
 *
 */
public class EditorForLiteral extends AnnotationLiteral<EditorFor> implements EditorFor {
  private final Class<?> typeClass;

  public EditorForLiteral(Class<?> typeClass) {
    this.typeClass = typeClass;
  }

  @Override
  public Class<?> value() {
    return typeClass;
  }
}
