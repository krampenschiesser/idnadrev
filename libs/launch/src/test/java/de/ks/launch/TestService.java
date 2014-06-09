/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks.launch;

import java.util.concurrent.CyclicBarrier;

public abstract class TestService extends Service {
  public CyclicBarrier barrier = new CyclicBarrier(2);
  private boolean fail = false;

  public void fail() {
    fail = true;
  }

  @Override
  protected void doStart() {
    await();
    if (fail) {
      throw new RuntimeException("intentionally failing");
    }
  }

  public void await() {
    try {
      barrier.await();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doStop() {
    await();
  }

}

