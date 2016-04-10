/*
 * Copyright [2016] [Christian Loehnert]
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
package de.ks.idnadrev.index;

import de.ks.idnadrev.adoc.AdocFile;
import org.reflections.ReflectionUtils;

import java.util.*;
import java.util.function.Predicate;

public class MultiQueyBuilder<E extends AdocFile> {
  private final Index index;
  private final Class<E> resultClass;
  protected LinkedHashMap<Query<E, Object>, Predicate<?>> queries = new LinkedHashMap<>();

  public MultiQueyBuilder(Index index, Class<E> resultClass) {
    this.index = index;
    this.resultClass = resultClass;
  }

  public <V> MultiQueyBuilder<E> queryNonNull(Query<E, V> query, Predicate<V> filter) {
    return query(query, filter, false);
  }

  public <V> MultiQueyBuilder<E> query(Query<E, V> query, Predicate<V> filter) {
    return query(query, filter, true);
  }

  @SuppressWarnings("unchecked")
  <V> MultiQueyBuilder<E> query(Query<E, V> query, Predicate<V> filter, boolean allowNull) {
    boolean isSubtype = resultClass.isAssignableFrom(query.getOwnerClass());
    boolean isSuperType = ReflectionUtils.getAllSuperTypes(resultClass).contains(query.getOwnerClass());

    if (isSubtype || isSuperType) {
      Query<E, Object> cast = (Query<E, Object>) query;
      if (allowNull) {
        queries.put(cast, (Predicate<Object>) filter);
      } else {
        Predicate<Object> nonNull = Objects::nonNull;
        queries.put(cast, nonNull.and((Predicate<Object>) filter));
      }
      return this;
    } else {
      throw new IllegalArgumentException("Given query class " + query.getOwnerClass() + " and expected result class " + resultClass + " are totally unrelated.");
    }
  }

  @SuppressWarnings("unchecked")
  public Set<E> find() {
    HashMap<E, Map<Query<E, Object>, Object>> indexElementMapHashMap = new HashMap<>();
    queries.keySet().forEach(query -> {
      Map<E, ? extends Optional<?>> queryElements = index.getQueryElements(query);

      queryElements.entrySet().stream()//
        .filter(e -> resultClass.isAssignableFrom(e.getKey().getClass()))//
        .forEach(entry -> {
          Map<Query<E, Object>, Object> map = indexElementMapHashMap.compute(entry.getKey(), (indexElement, queryObjectMap) -> queryObjectMap == null ? new HashMap<>() : queryObjectMap);
          map.put(query, entry.getValue().orElse(null));
        });
    });

    HashSet<E> results = new HashSet<>();
    for (Map.Entry<E, Map<Query<E, Object>, Object>> entry : indexElementMapHashMap.entrySet()) {
      boolean include = true;
      Map<Query<E, Object>, Object> value = entry.getValue();
      for (Map.Entry<Query<E, Object>, Object> queryObjectEntry : value.entrySet()) {
        Predicate predicate = queries.get(queryObjectEntry.getKey());
        include = predicate.test(queryObjectEntry.getValue());
        if (!include) {
          break;
        }
      }
      if (include) {
        results.add(entry.getKey());
      }
    }
    return results;
  }
}
