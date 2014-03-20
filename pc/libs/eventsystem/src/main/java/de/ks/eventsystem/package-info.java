/*
 * Copyright [2014] [Christian Loehnert]
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

/**
 * The EventSystem has the following requirements:
 * <ul>
 *   <li>Mainly similar to guava EventBus</li>
 *   <li>Multiple EventBuses possible.</li>
 *   <li>Each EventBus offers methods to register and unregister handlers as well as post to post an event</li>
 *   <li>Handlers are registered as WeakReference</li>
 *   <li>You can specify the target for an event by annotation:<br/>
 *    * CDI: will forward the event to CDI                               <br/>
 *    * JFX: will forward the event to the current JavaFX Stage         <br/>
 *    * None: default case                                             <br/>
 *   </li>
 *   <li>You can also specify the threads for an event by annotation:<br/>
 *    * Same: default case for synchronous invocation
 *    * JFX: will execute the event in the JFX thread
 *    * Async: will execute the event in the default work stealing executor.
 *   </li>
 *   <li>
 *     In the thread annotation you can specify if the handlers should wait for the event or not.
 *   </li>
 *   <li>An event can be consumed, so it will not be propagated to further handlers.
 *   The consumption occurs if the handler method returns a boolean which is set to true.
 *   </li>
 *   <li>
 *     Sorting of handlers via @Priority marker
 *   </li>
 * </ul>
 */
package de.ks.eventsystem;