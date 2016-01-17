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
package de.ks.idnadrev.entity.information;

import de.ks.flatadocdb.annotation.Entity;
import de.ks.flatadocdb.annotation.QueryProvider;
import de.ks.flatadocdb.query.Query;
import de.ks.idnadrev.entity.AdocContainerLuceneExtractor;

import java.time.LocalDate;

@Entity(luceneDocExtractor = AdocContainerLuceneExtractor.class)
public class DiaryInfo extends Information {
  @QueryProvider
  public static Query<DiaryInfo, LocalDate> dateQuery() {
    return Query.of(DiaryInfo.class, DiaryInfo::getDate);
  }

  protected LocalDate date;

  protected DiaryInfo() {
  }

  public DiaryInfo(LocalDate date) {
    super(DiaryInfo.class.getSimpleName() + "-" + date);
    this.date = date;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

}
