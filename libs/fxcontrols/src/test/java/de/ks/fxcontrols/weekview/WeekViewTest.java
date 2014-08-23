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

package de.ks.fxcontrols.weekview;

import de.ks.FXTestRunner;
import de.ks.util.FXPlatform;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.WeekFields;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

@RunWith(FXTestRunner.class)
public class WeekViewTest {

  @Test
  public void testWeek0() throws Exception {
    WeekView<Object> weekView = new WeekView<>("today");
    weekView.setYear(2014);
    weekView.setWeekOfYear(22);
    FXPlatform.invokeLater(() -> weekView.weekOfYear.set(0));

    Year previous = Year.of(2014).minusYears(1);
    LocalDate lastDayInYear = LocalDate.ofYearDay(previous.getValue(), previous.isLeap() ? 366 : 365);
    int weekOfYear = lastDayInYear.get(WeekFields.ISO.weekOfWeekBasedYear());
    weekOfYear = weekOfYear == 1 ? 52 : weekOfYear;
    assertEquals(weekOfYear, weekView.getWeekOfYear());

    assertEquals(previous.getValue(), weekView.getYear());
  }

  @Test
  public void testAppointmentCreation() throws Exception {
    WeekView<Object> weekView = new WeekView<>("today");
    weekView.setYear(2014);
    weekView.setWeekOfYear(22);

    LinkedList<LocalDateTime> times = new LinkedList<>();
    weekView.setOnAppointmentCreation((date, time) -> times.add(LocalDateTime.of(date, time)));

    FXPlatform.invokeLater(() -> weekView.getCells().get(13, 4).getOnMouseClicked().handle(null));
    assertEquals(1, times.size());
    assertEquals(LocalDateTime.of(2014, 5, 29, 13, 0), times.getLast());
  }
}