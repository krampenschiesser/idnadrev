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
package de.ks.idnadrev.expimp.xls.sheet;

import org.apache.poi.ss.usermodel.DateUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class LocalDateTimeValueParser extends ImportValueParser<LocalDateTime> {
  @Override
  public LocalDateTime getValue() {
    Double dVal = Double.valueOf(builder.toString());
    Date date = DateUtil.getJavaDate(dVal);
    return new Timestamp(date.getTime()).toLocalDateTime();
  }
}
