package de.ks.beagle.thought;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.ModelBound;
import de.ks.beagle.entity.Thought;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;

/**
 *
 */
@ModelBound(Thought.class)
public class AddThought {
  @FXML
  protected HTMLEditor description;
  @FXML
  protected TextField name;
}
