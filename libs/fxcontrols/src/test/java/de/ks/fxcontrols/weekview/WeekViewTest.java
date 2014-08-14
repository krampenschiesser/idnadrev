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
import java.time.Year;
import java.time.temporal.WeekFields;

import static org.junit.Assert.assertEquals;

@RunWith(FXTestRunner.class)
public class WeekViewTest {

  @Test
  public void testGetFirstDayOfWeek() throws Exception {
    WeekView weekView = new WeekView();

    assertEquals(LocalDate.of(2014, 8, 4), weekView.getFirstDayOfWeek(2014, 32));
    assertEquals(LocalDate.of(2013, 12, 23), weekView.getFirstDayOfWeek(2013, 52));
    assertEquals(LocalDate.of(2013, 12, 30), weekView.getFirstDayOfWeek(2014, 1));
    assertEquals(LocalDate.of(2014, 1, 6), weekView.getFirstDayOfWeek(2014, 2));
  }

  @Test
  public void testWeek0() throws Exception {
    WeekView weekView = new WeekView();
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
}