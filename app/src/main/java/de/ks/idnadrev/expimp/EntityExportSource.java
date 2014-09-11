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

import de.ks.persistence.entity.AbstractPersistentObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EntityExportSource<T extends AbstractPersistentObject> implements Iterable<T>, AutoCloseable {
  protected final List<EntityExportIterator<T>> iterators = new LinkedList<>();
  protected final Config config = new Config();
  private final Collection<Long> ids;
  private final Class<T> root;
  protected int bulkSize = 100;

  public EntityExportSource(Collection<Long> ids, Class<T> root) {
    this.ids = ids;
    this.root = root;
  }

  public Collection<Long> getIds() {
    return ids;
  }

  public Class<T> getRoot() {
    return root;
  }

  public String getIdentifier() {
    return root.getName();
  }

  public void setBulkSize(int bulkSize) {
    this.bulkSize = bulkSize;
  }

  public int getBulkSize() {
    return bulkSize;
  }

  @Override
  public Iterator<T> iterator() {
    return new EntityExportIterator<T>(bulkSize, ids, root);
  }

  public Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  @Override
  public void close() throws Exception {
    iterators.forEach(i -> i.close());
  }

  public Config getConfig() {
    return config;
  }

  public static class Config {
    protected List<String> ignoredFields = new LinkedList<>();

    public Config ignoreField(String name) {
      ignoredFields.add(name);
      return this;
    }

    public List<String> getIgnoredFields() {
      return ignoredFields;
    }
  }
}
