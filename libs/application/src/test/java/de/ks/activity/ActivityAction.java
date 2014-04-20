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

package de.ks.activity;


import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class ActivityAction extends Task<Void> {
  private static final Logger log = LoggerFactory.getLogger(ActivityAction.class);
  protected boolean executed = false;

  @Override
  protected Void call() throws Exception {
    this.executed = true;
    log.info("Pressed test button {}", ActivityAction.class.getSimpleName());
    return null;
  }

  public boolean isExecuted() {
    return executed;
  }
}
