package de.ks.beagle.thought;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.context.ActivityStore;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 *
 */
public class SaveThought extends Task<Void> {
  private static final Logger log = LoggerFactory.getLogger(SaveThought.class);

  @Inject
  ActivityStore context;

  @Override
  protected Void call() throws Exception {
    Object model = context.getModel();

    log.info("Saving thought, yeah baby!");
    return null;
  }
}
