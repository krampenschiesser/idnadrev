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


/**
 * Kind of an interceptor that is used to propagate values
 * from the calling thread to the executing thread.
 * <p>
 * You can use this eg. to read a ThreadLocal from the caller thread,
 * set it in the target thread, and unset it after execution.
 * (big example:CDI-Scope transmition.
 */
public interface ThreadCallBoundValue extends Cloneable {
  void initializeInCallerThread();

  void doBeforeCallInTargetThread();

  void doAfterCallInTargetThread();

  ThreadCallBoundValue clone();
}
