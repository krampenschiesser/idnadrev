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
import de.ks.idnadrev.task.Task;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class Index {
  final ConcurrentHashMap<Query, ConcurrentHashMap<AdocFile, Optional<Object>>> queryElements = new ConcurrentHashMap<>();

  final Collection<Query> queries;
  final Set<Task> tasks = Collections.synchronizedSet(new HashSet<>());
  final Set<AdocFile> adocFiles = Collections.synchronizedSet(new HashSet<>());
  final Map<Path, AdocFile> adocPaths = new ConcurrentHashMap<>();

  @Inject
  public Index(Set<Query> queries) {
    this.queries = queries;
  }

  @SuppressWarnings("unchecked")
  public Index add(AdocFile adocFile) {
    adocFiles.add(adocFile);
    adocPaths.put(adocFile.getPath(), adocFile);
    if (adocFile instanceof Task) {
      tasks.add((Task) adocFile);
    }
    for (Query query : queries) {
      if (query.getOwnerClass().isAssignableFrom(adocFile.getClass())) {
        @SuppressWarnings("unchecked")
        Object value = query.getValue(adocFile);
        ConcurrentHashMap<AdocFile, Optional<Object>> map = queryElements.computeIfAbsent(query, q -> new ConcurrentHashMap<>());
        map.put(adocFile, Optional.ofNullable(value));
      }
    }
    return this;
  }

  public void remove(AdocFile adocFile) {
    if (adocFile instanceof Task) {
      tasks.remove(adocFile);
    }
    adocFiles.remove(adocFile);
    adocPaths.remove(adocFile.getPath());
    for (ConcurrentHashMap<AdocFile, Optional<Object>> map : queryElements.values()) {
      map.remove(adocFile);
    }
  }

  public void update(AdocFile adocFile) {
    remove(adocFile);
    add(adocFile);
  }

  public <E extends AdocFile, V> Map<E, Optional<V>> getQueryElements(Query<E, V> query) {
    @SuppressWarnings("unchecked")
    ConcurrentHashMap<E, Optional<V>> retval = (ConcurrentHashMap) queryElements.get(query);
    return retval == null ? Collections.emptyMap() : retval;
  }

  public <R extends AdocFile, V> Collection<R> queryNonNull(Class<R> resultClass, Query<R, V> query, Predicate<V> filter) {
    MultiQueyBuilder<R> builder = multiQuery(resultClass);
    return builder.queryNonNull(query, filter).find();
  }

  public <R extends AdocFile, V> Collection<R> query(Class<R> resultClass, Query<R, V> query, Predicate<V> filter) {
    MultiQueyBuilder<R> builder = multiQuery(resultClass);
    return builder.query(query, filter).find();
  }

  public <E extends AdocFile, V> Set<V> queryValues(Query<E, V> query, Predicate<V> filter) {
    Map<E, Optional<V>> elements = getQueryElements(query);
    Set<Object> collected = elements.entrySet().stream()//
      .filter((entry) -> entry.getValue().isPresent())//
      .filter((entry) -> filter.test(entry.getValue().get()))//
      .map((entry) -> ((Optional) entry.getValue()).get())//
      .collect(Collectors.toSet());

    @SuppressWarnings("unchecked")
    Set<V> retval = (Set<V>) collected;
    return retval;
  }

  public <E extends AdocFile> MultiQueyBuilder<E> multiQuery(Class<E> resultClass) {
    return new MultiQueyBuilder<>(this, resultClass);
  }

  public boolean containsAdocPath(Path path) {
    return adocPaths.containsKey(path);
  }

  public AdocFile getAdocFile(Path path) {
    return adocPaths.get(path);
  }

  public List<AdocFile> getAdocFiles() {
    return new ArrayList<>(adocFiles);
  }

}
