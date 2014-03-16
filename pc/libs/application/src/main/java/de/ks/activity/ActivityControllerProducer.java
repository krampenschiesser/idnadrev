package de.ks.activity;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.inject.Produces;

/**
 *
 */
public class ActivityControllerProducer {
  @Produces
  public ActivityController create() {
    return ActivityController.instance;
  }
}
