package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ReceivingHandler {
  private Child child;
  private Parent parent;
  private AtomicInteger sum = new AtomicInteger();

  @Subscribe
  public void onParent(Parent parent) {
    this.parent = parent;
  }

  @Subscribe
  public void onChild(Child child) {
    this.child = child;
  }

  @Threading(HandlingThread.Async)
  @Subscribe
  public void onInt(int i) {
    try {
      Thread.sleep(sum.addAndGet(i));
    } catch (InterruptedException e) {
      //
    }
  }

  public int getSum() {
    return sum.get();
  }

  public Child getChild() {
    return child;
  }

  public Parent getParent() {
    return parent;
  }
}
