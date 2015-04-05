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
package de.ks.idnadrev.entity;

import de.ks.idnadrev.expimp.DependencyGraph;
import de.ks.idnadrev.expimp.ToOneRelation;
import de.ks.persistence.PersistentWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Cleanup {
  private static final Logger log = LoggerFactory.getLogger(Cleanup.class);

  @Inject
  DependencyGraph graph;

  public void cleanup() {
    Set<String> joinTables = graph.getJoinTables();
    PersistentWork.deleteJoinTables(joinTables);


    List<ToOneRelation> optionalToOneRelations = graph.getOptionalToOneRelations();
    for (ToOneRelation relation : optionalToOneRelations) {
      PersistentWork.run(em -> {
        Class owner = relation.getDeclaringClass();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

        CriteriaUpdate update = criteriaBuilder.createCriteriaUpdate(owner);

        @SuppressWarnings("unchecked") Root root = update.from(owner);
        Expression nullLiteral = criteriaBuilder.nullLiteral(relation.getRelationClass());
        Path path = root.get(relation.getName());
        update.set(path, nullLiteral);

        int i = em.createQuery(update).executeUpdate();
        if (i > 0) {
          log.debug("Set {} references {}.{} to null", i, owner.getName(), relation.getName());
        }
      });
    }


    List<Collection<EntityType<?>>> importOrder = graph.getImportOrder();
    Collections.reverse(importOrder);
    for (Collection<EntityType<?>> entityTypes : importOrder) {
      List<Class<?>> toDelete = entityTypes.stream().map(t -> t.getJavaType()).collect(Collectors.toList());
      PersistentWork.deleteAllOf(toDelete);
    }
  }
}
