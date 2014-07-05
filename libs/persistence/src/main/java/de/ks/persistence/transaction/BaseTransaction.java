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
package de.ks.persistence.transaction;

import java.util.ArrayList;
import java.util.List;

class BaseTransaction implements SimpleTransaction {
  private final String name;
  private List<Synchronization> synchronizations = new ArrayList<>();
  private List<TransactionResource> resources = new ArrayList<>();

  public BaseTransaction(String name) {
    this.name = name;
  }

  @Override
  public void prepare() {
    synchronizations.forEach(s -> s.beforeCompletion());
    resources.forEach(r -> r.prepare());
  }

  @Override
  public void commit() {
    resources.forEach(r -> r.commit());
    synchronizations.forEach(s -> s.afterSuccessfulCompletion());
  }

  @Override
  public void rollback() {
    resources.forEach(r -> r.rollback());
    synchronizations.forEach(s -> s.afterFailedCompletion());
  }

  @Override
  public void registerSynchronization(Synchronization synchronization) {
    synchronizations.add(synchronization);
  }

  public void registerResource(TransactionResource resource) {
    this.resources.add(resource);
  }

  public String getName() {
    return name;
  }

  public List<TransactionResource> getResources() {
    return resources;
  }

  @Override
  public String toString() {
    return "BaseTransaction{" +
            "name='" + name + '\'' +
            '}';
  }
}
