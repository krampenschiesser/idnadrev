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
package de.ks.idnadrev.adoc.view;

import de.ks.idnadrev.adoc.AdocFile;
import de.ks.idnadrev.index.Index;
import de.ks.idnadrev.repository.Repository;
import de.ks.standbein.datasource.ListDataSource;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ViewAdocDs implements ListDataSource<AdocFile> {
  @Inject
  Index index;

  private ViewAdocFilter viewAdocFilter;

  @Override
  public List<AdocFile> loadModel(Consumer<List<AdocFile>> furtherProcessing) {
    List<AdocFile> found = index.getAdocFiles();
    if (viewAdocFilter != null) {
      found = found.parallelStream().filter(viewAdocFilter).collect(Collectors.toList());
    }
    List<AdocFile> sorted = new ArrayList<>(found);
    Collections.sort(sorted, Comparator.comparing(AdocFile::getTitle));
    return sorted;
  }

  @Override
  public void saveModel(List<AdocFile> model, Consumer<List<AdocFile>> beforeSaving) {

  }

  @Override
  @SuppressWarnings("unchecked")
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof ViewAdocFilter) {
      viewAdocFilter = (ViewAdocFilter) dataSourceHint;
    }
  }

  public static class ViewAdocFilter implements Predicate<AdocFile> {
    final ArrayList<Predicate<AdocFile>> filters = new ArrayList<>();

    public ViewAdocFilter(String title, List<String> selectedTags, String repository, String wildCard) {
      Objects.requireNonNull(title);
      Objects.requireNonNull(repository);
      Objects.requireNonNull(wildCard);

      if (!title.isEmpty()) {
        filters.add(t -> t.getTitle().toLowerCase(Locale.ROOT).trim().contains(title));
      }
      if (!selectedTags.isEmpty()) {
        filters.add(t -> !Collections.disjoint(t.getHeader().getTags(), selectedTags));
      }
      if (!repository.isEmpty() && !wildCard.equals(repository)) {
        filters.add(t -> Optional.ofNullable(t.getRepository()).map(Repository::getName).map(s -> s.toLowerCase(Locale.ROOT).trim()).orElse("").equals(repository));
      }
    }

    @Override
    public boolean test(AdocFile adocFile) {
      for (Predicate<AdocFile> filter : filters) {
        if (!filter.test(adocFile)) {
          return false;
        }
      }
      return true;
    }
  }
}
