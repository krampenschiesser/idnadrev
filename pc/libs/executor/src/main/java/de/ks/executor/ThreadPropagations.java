/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.executor;


import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ThreadPropagations {
  private HashSet<ThreadCallBoundValue> propagations = new HashSet<>();

  public void register(ThreadCallBoundValue value) {
    propagations.add(value);
  }

  public Set<ThreadCallBoundValue> getPropagations() {
    HashSet<ThreadCallBoundValue> retval = new HashSet<>();
    for (ThreadCallBoundValue propagation : propagations) {
      retval.add(propagation.clone());
    }
    return retval;
  }
}
