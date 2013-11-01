package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;

/**
 *
 */
public class Handler {
  @Subscribe
  public void invalidHandler(Object bla, Object blubb) {

  }

  @Priority(1)
  @Subscribe
  private void validHandler(Object event) {

  }


  @Subscribe
  protected boolean validConsumingHandler(Object event) {
    return true;
  }

  @Priority(2)
  @Subscribe
  protected boolean validNonConsumingHandler(Object event) {
    return false;
  }

}
