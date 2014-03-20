/*
 * Copyright [${YEAR}] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ReceivingHandler {
  private Child child;
  private Parent parent;
  private AtomicInteger sum = new AtomicInteger();
  private final CountDownLatch latch;

  public ReceivingHandler(int count) {
    latch = new CountDownLatch(count);
  }

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
      latch.countDown();
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

  public CountDownLatch getLatch() {
    return latch;
  }
}
