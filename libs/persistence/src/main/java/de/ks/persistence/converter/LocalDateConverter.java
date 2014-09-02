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

package de.ks.persistence.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;

/**
 *
 */
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {
  @Override
  public Date convertToDatabaseColumn(LocalDate attribute) {
    if (attribute == null) {
      return null;
    } else {
      return Date.valueOf(attribute);
    }
  }

  @Override
  public LocalDate convertToEntityAttribute(Date dbData) {
    if (dbData == null) {
      return null;
    } else {
      return dbData.toLocalDate();
    }
  }
}
