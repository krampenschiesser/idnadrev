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

import de.ks.persistence.entity.IdentifyableEntity;
import de.ks.reflection.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.EntityManagerFactory;
import javax.persistence.JoinTable;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DependencyGraph {
  private static final Logger log = LoggerFactory.getLogger(DependencyGraph.class);
  private final Map<Class<?>, String> class2IdentifierPropertyName = new ConcurrentHashMap<>();
  @Inject
  EntityManagerFactory factory;

  protected final List<Collection<EntityType<?>>> importOrder = new ArrayList<>();

  public List<Collection<EntityType<?>>> getImportOrder() {
    if (importOrder.isEmpty()) {
      Set<EntityType<?>> entities = new HashSet<>(factory.getMetamodel().getEntities());

      while (!entities.isEmpty()) {
        List<EntityType<?>> rootEntites = getNextEntities(entities, importOrder);
        importOrder.add(rootEntites);
      }
      log.debug("Found {} waves to import", importOrder.size());
      for (int i = 0; i < importOrder.size(); i++) {
        log.debug("Found import wave {} objects {}", i, importOrder.get(i).stream().map(t -> t.getJavaType().getName()).collect(Collectors.toList()));
      }
    }

    ArrayList<Collection<EntityType<?>>> retval = new ArrayList<>(importOrder.size());
    importOrder.forEach(i -> retval.add(new ArrayList<>(i)));
    return retval;
  }

  public int getStage(Class<?> entity) {
    List<Collection<EntityType<?>>> order = getImportOrder();
    for (int i = 0; i < order.size(); i++) {
      Optional<EntityType<?>> first = order.get(i).stream().filter(t -> t.getJavaType().equals(entity)).findFirst();
      if (first.isPresent()) {
        return i;
      }
    }
    throw new IllegalArgumentException(entity.getName() + " is not contained in any stage");
  }

  public EntityType<?> getEntityType(Class<?> entity) {
    List<Collection<EntityType<?>>> order = getImportOrder();
    for (int i = 0; i < order.size(); i++) {
      Optional<EntityType<?>> first = order.get(i).stream().filter(t -> t.getJavaType().equals(entity)).findFirst();
      if (first.isPresent()) {
        return first.get();
      }
    }
    throw new IllegalArgumentException(entity.getName() + " is not contained in any stage");
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

  public String getIdentifierProperty(Class<?> clazz) {
    return class2IdentifierPropertyName.computeIfAbsent(clazz, c -> {
      if (IdentifyableEntity.class.isAssignableFrom(c)) {
        @SuppressWarnings("unchecked") IdentifyableEntity entity = ReflectionUtil.newInstance((Class<? extends IdentifyableEntity>) c);
        return entity.getIdPropertyName();
      }
      return null;
    });
  }

  public Set<String> getJoinTables() {
    Set<String> retval = new HashSet<>();
    for (EntityType<?> entityType : factory.getMetamodel().getEntities()) {
      retval.addAll(getOverridenJoinTables(entityType));

      for (PluralAttribute<?, ?, ?> pluralAttribute : entityType.getPluralAttributes()) {
        if (pluralAttribute.isAssociation()) {
          Member javaMember = pluralAttribute.getJavaMember();
          if (javaMember instanceof Field) {
            JoinTable annotation = ((Field) javaMember).getAnnotation(JoinTable.class);
            if (annotation != null) {
              String name = annotation.name();
              if (name != null && !name.isEmpty()) {
                retval.add(name);
              }
            }
          }
        }
      }
    }
    return retval;
  }

  private HashSet<String> getOverridenJoinTables(EntityType<?> entityType) {
    HashSet<String> retval = new HashSet<>();
    AssociationOverrides collection = entityType.getJavaType().getAnnotation(AssociationOverrides.class);
    if (collection != null) {
      for (AssociationOverride associationOverride : collection.value()) {
        JoinTable joinTable = associationOverride.joinTable();
        if (joinTable != null) {
          String name = joinTable.name();
          if (name != null && !name.isEmpty()) {
            retval.add(joinTable.name());
          }
        }
      }
    }
    return retval;
  }

  public List<ToOneRelation> getOptionalToOneRelations() {
    List<ToOneRelation> retval = new LinkedList<>();
    for (EntityType<?> entityType : factory.getMetamodel().getEntities()) {
      if (entityType.getPersistenceType() == Type.PersistenceType.ENTITY) {
        List<ToOneRelation> optionalRelations = entityType.getSingularAttributes().stream()//
                .filter(a -> a.isAssociation() && a.isOptional())//
                .map(r -> new ToOneRelation(entityType, r))//
                .collect(Collectors.toList());
        retval.addAll(optionalRelations);
      }
    }
    return retval;
  }
}
