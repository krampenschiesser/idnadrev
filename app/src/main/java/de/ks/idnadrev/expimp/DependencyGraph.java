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
package de.ks.idnadrev.expimp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyGraph {
  private static final Logger log = LoggerFactory.getLogger(DependencyGraph.class);

  @Inject
  EntityManagerFactory factory;

  public List<Collection<EntityType<?>>> getImportOrder() {
    Set<EntityType<?>> entities = new HashSet<>(factory.getMetamodel().getEntities());

    List<Collection<EntityType<?>>> importOrder = new ArrayList<>();
    while (!entities.isEmpty()) {
      List<EntityType<?>> rootEntites = getNextEntities(entities, importOrder);
      importOrder.add(rootEntites);
    }
    log.debug("Found {} waves to import", importOrder.size());
    for (int i = 0; i < importOrder.size(); i++) {
      log.debug("Found import wave {} objects {}", i, importOrder.get(i).stream().map(t -> t.getJavaType().getName()).collect(Collectors.toList()));
    }
    return importOrder;
  }

  private List<EntityType<?>> getNextEntities(Set<EntityType<?>> entities, List<Collection<EntityType<?>>> importOrder) {
    List<EntityType<?>> roots = entities.stream()//
            .filter(e -> {
              @SuppressWarnings("unchecked") Optional mandatoryRelation = e.getSingularAttributes().stream().filter(a -> a.isAssociation() && !a.isOptional()).findFirst();
              if (!mandatoryRelation.isPresent()) {
                return true;
              } else {
                @SuppressWarnings("unchecked") SingularAttribute relation = (SingularAttribute) mandatoryRelation.get();
                boolean entityContained = importOrder.stream().filter(l -> l.stream().filter(type -> type.getJavaType().equals(relation.getJavaType())).findFirst().isPresent()).findFirst().isPresent();
                return entityContained;
              }
            }).collect(Collectors.toList());
    entities.removeAll(roots);
    return roots;
  }
}
