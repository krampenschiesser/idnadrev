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

package de.ks.idnadrev.task.cron;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class WeekHelperTest {
  @Test
  public void testGerman() throws Exception {
    Locale.setDefault(Locale.GERMAN);

    LocalDate monday = new WeekHelper().getFirstDayOfWeek(2016, 1);
    assertEquals(LocalDate.of(2016, 1, 4), monday);

    assertEquals(1, new WeekHelper().getWeek(LocalDate.of(2016, 1, 4)));
    assertEquals(53, new WeekHelper().getWeek(LocalDate.of(2016, 1, 1)));
    assertEquals(52, new WeekHelper().getWeek(LocalDate.of(2016, 12, 29)));
  }

  @Test
  public void testEnglish() throws Exception {
    Locale.setDefault(Locale.ENGLISH);

    LocalDate sunday = new WeekHelper().getFirstDayOfWeek(2016, 1);
    assertEquals(LocalDate.of(2015, 12, 27), sunday);
    assertEquals(DayOfWeek.SUNDAY, sunday.getDayOfWeek());

    assertEquals(1, new WeekHelper().getWeek(LocalDate.of(2016, 1, 1)));
    assertEquals(1, new WeekHelper().getWeek(LocalDate.of(2015, 12, 28)));
    assertEquals(53, new WeekHelper().getWeek(LocalDate.of(2016, 12, 29)));
  }
}