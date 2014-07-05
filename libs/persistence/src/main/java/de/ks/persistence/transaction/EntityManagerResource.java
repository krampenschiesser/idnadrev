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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

class EntityManagerResource implements TransactionResource {
  private final EntityManager em;
  private EntityTransaction transaction;

  public EntityManagerResource(EntityManager em) {
    this.em = em;
    transaction = em.getTransaction();
    transaction.begin();
  }

  @Override
  public void prepare() {
    em.flush();
  }

  @Override
  public void commit() {
    transaction.commit();
    transaction = null;
  }

  @Override
  public void rollback() {
    if (transaction != null) {
      transaction.rollback();
    }
  }

  @Override
  public void close() {
    em.close();
  }
}
