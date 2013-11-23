package de.ks.editor;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.Grid2DEditorProvider;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 *
 */
public class StringEditor implements Grid2DEditorProvider<Label,TextField> {



  @Override
  public TextField get() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Label getDescriptor() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
