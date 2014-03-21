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
