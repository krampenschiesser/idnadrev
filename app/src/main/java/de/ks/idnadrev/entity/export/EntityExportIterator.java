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
package de.ks.idnadrev.entity.export;

import de.ks.persistence.entity.AbstractPersistentObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

class EntityExportIterator<T extends AbstractPersistentObject> implements Iterator<T>, AutoCloseable {
  private static final Logger log = LoggerFactory.getLogger(EntityExportIterator.class);
  final EntityManager em = CDI.current().select(EntityManager.class).get();
  private final int bulkSize;
  private final Collection<Long> ids;
  private final Class<T> rootClass;
  int pos = 0;
  final List<T> objects;

  public EntityExportIterator(int bulkSize, Collection<Long> ids, Class<T> root) {
    this.bulkSize = bulkSize;
    this.ids = ids;
    this.rootClass = root;
    objects = new ArrayList<>(bulkSize);
    em.setFlushMode(FlushModeType.COMMIT);
  }

  @Override
  public boolean hasNext() {
    boolean hasnext = pos < ids.size();
    if (!hasnext) {
      close();
    }
    return hasnext;
  }

  @Override
  public T next() {
    int currentIndex = pos % 100;
    if (currentIndex == 0) {
      em.clear();
      CriteriaQuery<T> criteriaQuery = em.getCriteriaBuilder().createQuery(rootClass);
      Root<T> from = criteriaQuery.from(rootClass);
      criteriaQuery.select(from);
      TypedQuery<T> query = em.createQuery(criteriaQuery);

      query.setFirstResult(pos);// I know scrolling would be better and safe, but! I want to stick to plain JPA
      query.setMaxResults(bulkSize);

      objects.clear();
      objects.addAll(query.getResultList());
    }
    if (currentIndex < objects.size()) {
      T retval = objects.get(currentIndex);
      pos++;
      return retval;
    } else {
      close();
    }
    throw new IllegalStateException("pos " + pos + " > " + objects.size());
  }

  public void close() {
    objects.clear();
    em.close();
  }
}
