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
package de.ks.idnadrev.information.diary;

import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.information.DiaryInfo;
import de.ks.persistence.PersistentWork;
import de.ks.reflection.PropertyPath;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class DiaryDS implements DataSource<DiaryInfo> {

  private LocalDate date = LocalDate.now();
  private Set<LocalDate> dates = new HashSet<>();

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof LocalDate) {
      date = ((LocalDate) dataSourceHint);
    }
  }

  @Override
  public DiaryInfo loadModel(Consumer<DiaryInfo> furtherProcessing) {
    dates.addAll(readAllDates());
    DiaryInfo read = getExistingDiaryInfo(furtherProcessing);
    if (read != null) {
      return read;
    } else {
      DiaryInfo instance = new DiaryInfo(date);
      furtherProcessing.accept(instance);
      return instance;
    }
  }

  public Set<LocalDate> getDates() {
    return dates;
  }

  private List<LocalDate> readAllDates() {
    return PersistentWork.read(em -> {
      CriteriaBuilder builder = em.getCriteriaBuilder();
      CriteriaQuery<LocalDate> query = builder.createQuery(LocalDate.class);
      Root<DiaryInfo> root = query.from(DiaryInfo.class);

      Path<LocalDate> datePath = root.get(PropertyPath.property(DiaryInfo.class, d -> d.getDate()));
      query.select(datePath);

      //restrict later

      List<LocalDate> resultList = em.createQuery(query).getResultList();
      return resultList;
    });
  }

  private DiaryInfo getExistingDiaryInfo(Consumer<DiaryInfo> furtherProcessing) {
    return PersistentWork.read(em -> {
      CriteriaBuilder builder = em.getCriteriaBuilder();
      CriteriaQuery<DiaryInfo> query = builder.createQuery(DiaryInfo.class);
      Root<DiaryInfo> root = query.from(DiaryInfo.class);
      query.select(root);

      Path<Object> datePath = root.get(PropertyPath.property(DiaryInfo.class, d -> d.getDate()));
      query.where(builder.equal(datePath, date));

      List<DiaryInfo> results = em.createQuery(query).getResultList();
      if (results.isEmpty()) {
        return null;
      } else {
        DiaryInfo loaded = results.get(0);
        furtherProcessing.accept(loaded);
        return loaded;
      }
    });
  }

  @Override
  public void saveModel(DiaryInfo model, Consumer<DiaryInfo> beforeSaving) {
    PersistentWork.run(em -> {
      DiaryInfo reloaded = PersistentWork.reload(model);
      beforeSaving.accept(reloaded);
      if (reloaded.getId() == 0 && !reloaded.getContent().trim().isEmpty()) {
        em.persist(reloaded);
        dates.add(model.getDate());
      }
    });
  }
}
