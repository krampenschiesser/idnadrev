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

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.WeekFields;

import static org.junit.Assert.assertEquals;

public class WeekHelperTest {
  WeekHelper helper = new WeekHelper();

  @Test
  public void testFirstWeekDayOfWeek1() throws Exception {
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2013), 31, Month.DECEMBER);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2014), 30, Month.DECEMBER);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2015), 29, Month.DECEMBER);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2016), 4, Month.JANUARY);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2017), 2, Month.JANUARY);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2018), 1, Month.JANUARY);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2019), 31, Month.DECEMBER);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2020), 30, Month.DECEMBER);
    checkFirstOfYear(helper.getFirstDayOfWeek1InYear(2021), 4, Month.JANUARY);
  }

  private void checkFirstOfYear(LocalDate date, int dayOfMonth, Month month) {
    assertEquals(WeekFields.ISO.getFirstDayOfWeek(), date.getDayOfWeek());
    String message = "expected " + dayOfMonth + "." + month.getValue() //
            + " but was " + date.getDayOfMonth() + "." + date.getMonth().getValue();
    assertEquals(message, month, date.getMonth());
    assertEquals(message, dayOfMonth, date.getDayOfMonth());
  }

  @Test
  public void testFirstDayOfWeek() throws Exception {
    checkFirstOfWeek(helper.getFirstDayOfWeek(LocalDate.of(2013, 1, 3)));
    checkFirstOfWeek(helper.getFirstDayOfWeek(LocalDate.of(2014, 1, 3)));
    checkFirstOfWeek(helper.getFirstDayOfWeek(LocalDate.of(2015, 1, 3)));
    checkFirstOfWeek(helper.getFirstDayOfWeek(LocalDate.of(2016, 1, 3)));
    checkFirstOfWeek(helper.getFirstDayOfWeek(LocalDate.of(2017, 1, 3)));

    assertEquals(LocalDate.of(2013, 1, 28), helper.getFirstDayOfWeek(LocalDate.of(2013, 1, 30)));
    assertEquals(LocalDate.of(2013, 2, 4), helper.getFirstDayOfWeek(LocalDate.of(2013, 2, 7)));
    assertEquals(LocalDate.of(2013, 2, 11), helper.getFirstDayOfWeek(LocalDate.of(2013, 2, 14)));

    assertEquals(LocalDate.of(2014, 8, 4), helper.getFirstDayOfWeek(2014, 32));
    assertEquals(LocalDate.of(2013, 12, 23), helper.getFirstDayOfWeek(2013, 52));
    assertEquals(LocalDate.of(2013, 12, 30), helper.getFirstDayOfWeek(2014, 1));
    assertEquals(LocalDate.of(2014, 1, 6), helper.getFirstDayOfWeek(2014, 2));
  }

  @Test
  public void testLastDayOfWeek() throws Exception {
    checkLastOfWeek(helper.getLastDayOfWeek(LocalDate.of(2013, 1, 3)));
    checkLastOfWeek(helper.getLastDayOfWeek(LocalDate.of(2014, 1, 3)));
    checkLastOfWeek(helper.getLastDayOfWeek(LocalDate.of(2015, 1, 3)));
    checkLastOfWeek(helper.getLastDayOfWeek(LocalDate.of(2016, 1, 3)));
    checkLastOfWeek(helper.getLastDayOfWeek(LocalDate.of(2017, 1, 3)));

    assertEquals(LocalDate.of(2013, 2, 3), helper.getLastDayOfWeek(LocalDate.of(2013, 1, 30)));
    assertEquals(LocalDate.of(2013, 2, 10), helper.getLastDayOfWeek(LocalDate.of(2013, 2, 7)));
    assertEquals(LocalDate.of(2013, 2, 17), helper.getLastDayOfWeek(LocalDate.of(2013, 2, 14)));
  }

  @Test
  public void testWeeksInYear() throws Exception {
    assertEquals(52, helper.getWeeksInYear(2013));
    assertEquals(52, helper.getWeeksInYear(2014));

    assertEquals(53, helper.getWeeksInYear(2015));

    assertEquals(52, helper.getWeeksInYear(2016));
    assertEquals(52, helper.getWeeksInYear(2017));
    assertEquals(52, helper.getWeeksInYear(2018));
    assertEquals(52, helper.getWeeksInYear(2019));

    assertEquals(53, helper.getWeeksInYear(2020));
  }

  private void checkFirstOfWeek(LocalDate date) {
    assertEquals(WeekFields.ISO.getFirstDayOfWeek(), date.getDayOfWeek());
  }

  private void checkLastOfWeek(LocalDate date) {
    assertEquals(7, date.getDayOfWeek().getValue());
  }

  @Test
  public void testMonthOfWeek() throws Exception {
    assertEquals(Month.JANUARY, helper.getMonthOfWeek(2013, 5));
    assertEquals(Month.FEBRUARY, helper.getMonthOfWeek(2013, 9));
  }
}