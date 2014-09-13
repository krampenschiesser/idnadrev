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

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

public class ToOneRelation {
  protected final EntityType<?> declaringType;
  protected final SingularAttribute<?, ?> relation;

  public ToOneRelation(EntityType<?> declaringType, SingularAttribute<?, ?> relation) {
    this.declaringType = declaringType;
    this.relation = relation;
  }

  public boolean isOptional() {
    return relation.isOptional();
  }

  public Class<?> getDeclaringClass() {
    return declaringType.getJavaType();
  }

  public Class<?> getRelationClass() {
    return relation.getJavaType();
  }

  public String getName() {
    return relation.getName();
  }

  public EntityType<?> getDeclaringType() {
    return declaringType;
  }

  public SingularAttribute<?, ?> getRelation() {
    return relation;
  }
}
