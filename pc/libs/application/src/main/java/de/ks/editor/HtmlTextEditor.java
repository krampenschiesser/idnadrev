package de.ks.editor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.scene.Node;
import javafx.scene.web.HTMLEditor;

/**
 *
 */
@Detailed
@EditorFor(String.class)
public class HtmlTextEditor extends AbstractEditor {
  protected HTMLEditor editor;

  @Override
  protected void initializeInJFXThread() {
    editor = new HTMLEditor();
  }

  @Override
  public Node getNode() {
    return editor;
  }
}
