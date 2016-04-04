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
package de.ks.idnadrev;

import de.ks.idnadrev.repository.Repository;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class GenericDateTimeParser {
  public LocalDateTime parse(String input, Repository repository) {
    if (input == null) {
      return null;
    }
    boolean considerSpace = false;
    LocalDate date;
    try {
      date = LocalDate.parse(input, repository.getDateFormatter());
    } catch (DateTimeParseException e) {
      considerSpace = true;
      date = LocalDate.parse(input.substring(0, input.indexOf(" ")), repository.getDateFormatter());
    }
    LocalTime time = LocalTime.of(0, 0);
    String timeString = considerSpace ? input.substring(input.indexOf(" ")).trim() : input;
    int colonAmount = StringUtils.countMatches(timeString, ":");
    if (colonAmount == 1) {
      timeString += ":00";
    }
    try {
      time = LocalTime.parse(timeString, repository.getTimeFormatter());
    } catch (DateTimeParseException e) {
      //ok
    }
    return LocalDateTime.of(date, time);
  }
}
