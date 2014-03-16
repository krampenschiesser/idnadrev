package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.inject.Produces;

/**
 *
 */
public class NavigatorProducer {

  @Produces
  public Navigator createNavigator() {
    return Navigator.getCurrentNavigator();
  }
}
