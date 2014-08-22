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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeekViewAppointmentTest {

  @Test
  public void testContains() throws Exception {
    LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0));
    WeekViewAppointment<Object> appointment = new WeekViewAppointment<>("test", start, Duration.ofMinutes(15));
    checkContains(appointment, LocalTime.of(12, 14));
    checkContains(appointment, LocalTime.of(12, 0));
    checkContains(appointment, LocalTime.of(12, 15));
    checkContainsNot(appointment, LocalTime.of(12, 16));
    checkContainsNot(appointment, LocalTime.of(11, 59));
  }

  private void checkContains(WeekViewAppointment<Object> appointment, LocalTime time) {
    LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), time);
    assertTrue(time + " should be contained", appointment.contains(localDateTime));
  }

  private void checkContainsNot(WeekViewAppointment<Object> appointment, LocalTime time) {
    LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), time);
    assertFalse(time + " should not be contained", appointment.contains(localDateTime));
  }
}