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

import de.ks.persistence.converter.LocalDateConverter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@AssociationOverrides(@AssociationOverride(name = "tags", joinTable = @JoinTable(name = "diaryinfo_tag")))
public class DiaryInfo extends Information<DiaryInfo> {
  private static final long serialVersionUID = 1L;
  @Column(columnDefinition = "DATE", unique = true)
  @Convert(converter = LocalDateConverter.class)
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
