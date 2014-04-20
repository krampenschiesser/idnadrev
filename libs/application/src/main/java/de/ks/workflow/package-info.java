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

/**
 * A workflow is basically a defined sequence of user input.
 * This can be a simple case like
 * <ul>
 *   <li>adding a new thought(create and persist entity)</li>
 *   <li>Editing a note</li>
 * </ul>
 * <pre>
 * Or something more complex like editing a task,
 * which would involve selecting a schedule with reoccurance and delegation(involves other entities).
 *
 * A workflow is based on a <b>model</b> which stores its state.
 * In a simple case this is an entity from the database(or freshly created) which is edited and persisted(or merged).
 * In a complex case this is an object which contains other objects.
 * But it always is a POJO!
 *
 * A workflow can transition into another one which will start a new workflow with a new model.
 *
 * The single steps of a workflow are defined by workflow hints or can be fully customized in their order.
 * For adding a new thought there would be a <code>new Thought()</code>.
 * There will be one mandatory step which instructs the user to provide a name and a description.
 * There will be an optional step which gives the user the possibility to convert the thought right
 * into a new action/project or information etc.
 * So this workflow will transition into, for example, task creation.
 *
 * For task creation the workflow will consist of more steps.
 * There will be one mandatory step which involves defining name,description,context,worktype and estimated time.
 * There will be optional steps for
 * </pre>
 * <ul>
 *   <li>Timing: Schedule and duedate</li>
 *   <li>Delegation to another person</li>
 *   <li>Adding it to a project</li>
 * </ul>
 *
 * Validation will occur via BeanValidation.
 */
package de.ks.workflow;