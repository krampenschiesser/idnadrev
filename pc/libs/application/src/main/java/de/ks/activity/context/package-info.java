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
 * use case data input:
 *
 * UI Thread:                       PooledThread-1              PooledThread-2
 *  input into textfields
 *  -> launches Validation          validate new Value
 *                                  validation failed
 *                                  <- show error
 * set new css on textfield
 * show popup with validation msg
 *
 * more input into textfield
 * -> launch validation             validate new value
 *                                  validation successful
 *                                  update model property
 *                                  <- remove validaiton error
 * remove css on textfield
 * hide popup if still visible
 *
 * press button
 * -> launch action                 validate model
 *                                  validation failed
 *                                  <- show error
 *                                  validation successful
 *                                  call datasource, submit model for saving
 *                                  reset activitystore (implementation specifc)
 *                                  bindings set to new instance
 *
 * reset property(will clear textfields)
 *
 *
 * textfield
 *  textproperty bindBidirectional to simplestringproperty
 *
 * simplestringproperty
 *  onChange->validate
 *
 * validate
 *   run validation for value
 *     on success
 *       write value back
 *       fireValueChanged on simpleStringProperty
 *
 *
 */
package de.ks.activity.context;