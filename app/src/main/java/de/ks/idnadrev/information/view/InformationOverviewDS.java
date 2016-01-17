/**
 * Copyright [2014] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.information.view;

import com.google.common.collect.Sets;
import de.ks.flatadocdb.entity.NamedEntity;
import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.TaggedEntity;
import de.ks.idnadrev.entity.information.DiaryInfo;
import de.ks.idnadrev.entity.information.Information;
import de.ks.standbein.datasource.ListDataSource;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InformationOverviewDS implements ListDataSource<Information> {
  protected volatile InformationLoadingHint loadingHint = new InformationLoadingHint("");

  @Inject
  PersistentWork persistentWork;

  @Override
  public List<Information> loadModel(Consumer<List<Information>> furtherProcessing) {
    String name = loadingHint.getName();
    Set<String> tags = loadingHint.getTags();

    return persistentWork.read(session -> {
      Collection<Information> information = persistentWork.multiQuery(Information.class, builder -> {
        if (!tags.isEmpty()) {
          builder.query(TaggedEntity.byTags(), current -> {
            Set<String> currentTagStrings = current.stream().map(Tag::getReducedName).collect(Collectors.toSet());
            return !Sets.intersection(tags, currentTagStrings).isEmpty();
          });
        }
        builder.query(NamedEntity.nameQuery(), current -> current.trim().toLowerCase(Locale.ROOT).contains(name));
      });
      List<Information> sorted = information.stream().filter(i -> !(i instanceof DiaryInfo)).collect(Collectors.toList());
      Collections.sort(sorted, Comparator.comparing(NamedEntity::getName));
      furtherProcessing.accept(sorted);

      return sorted;
    });
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof InformationLoadingHint) {
      this.loadingHint = (InformationLoadingHint) dataSourceHint;
    }
  }

  @Override
  public void saveModel(List<Information> model, Consumer<List<Information>> beforeSaving) {

  }
}
