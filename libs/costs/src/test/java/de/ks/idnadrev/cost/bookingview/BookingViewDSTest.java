/*
 * Copyright [2015] [Christian Loehnert]
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

package de.ks.idnadrev.cost.bookingview;

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.cost.entity.Account;
import de.ks.idnadrev.cost.entity.Booking;
import de.ks.idnadrev.cost.module.CostModule;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

//@RunWith(LauncherRunner.class)
public class BookingViewDSTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new CostModule(), new IntegrationTestModule());

  //  @Inject
//  Cleanup cleanup;
  private BookingViewDS datasource;

  @Inject
  PersistentWork persistentWork;

  @Before
  public void setUp() throws Exception {
//    cleanup.cleanup();

    persistentWork.run(em -> {
      Account account1 = new Account("testAccount1");
      persistentWork.persist(account1);
      int amount = 15;
      LocalDateTime dateTime = LocalDateTime.now().minusDays(amount);
      for (int i = 0; i < amount; i++) {
        Booking booking = new Booking(account1, new BigDecimal((i + 1) * 10));
        booking.setDescription("createbooking #" + i);
        booking.setCategory("Category" + i % 5);
        booking.setBookingTime(dateTime.plusDays(i));
        persistentWork.persist(booking);
      }
      Booking booking = new Booking(account1, new BigDecimal(-42));
      booking.setBookingTime(dateTime);
      persistentWork.persist(booking);
    });
    datasource = new BookingViewDS();
  }

  @Test
  public void testLoadAll() throws Exception {
    BookingLoadingHint hint = new BookingLoadingHint("testAccount1");
    datasource.setLoadingHint(hint);
    BookingViewModel model = datasource.loadModel(null);
    assertEquals(19, model.bookings.size());
  }

  @Test
  public void testDateFilter() throws Exception {
    BookingLoadingHint hint = new BookingLoadingHint("testAccount1");
    hint.setStartDate(hint.getStartDate().toLocalDate().plusDays(16));
    hint.setEndDate(hint.getEndDate().toLocalDate().minusDays(2));
    datasource.setLoadingHint(hint);
    BookingViewModel model = datasource.loadModel(null);
    assertEquals(16, model.bookings.size());
  }

  @Test
  public void testAmount() throws Exception {
    BookingLoadingHint hint = new BookingLoadingHint("testAccount1");
    hint.setAmount(20D);
    datasource.setLoadingHint(hint);
    BookingViewModel model = datasource.loadModel(null);
    assertEquals(4, model.bookings.size());
  }

  @Test
  public void testNegativeAmount() throws Exception {
    BookingLoadingHint hint = new BookingLoadingHint("testAccount1");
    hint.setAmount(-42D);
    datasource.setLoadingHint(hint);
    BookingViewModel model = datasource.loadModel(null);
    assertEquals(4, model.bookings.size());
    assertEquals(-42D, model.getBookings().get(1).getAmount().doubleValue(), 0.01D);
  }

  @Test
  public void testAmountDesc() throws Exception {
    BookingLoadingHint hint = new BookingLoadingHint("testAccount1");
    hint.setAmount(20D);
    hint.setDescription("book");
    datasource.setLoadingHint(hint);
    BookingViewModel model = datasource.loadModel(null);
    assertEquals(4, model.bookings.size());
    hint.setDescription("bla");
    model = datasource.loadModel(null);
    assertEquals(0, model.bookings.size());
  }

  @Test
  public void testCategory() throws Exception {
    BookingLoadingHint hint = new BookingLoadingHint("testAccount1");
    hint.setCategory("Category3");
    datasource.setLoadingHint(hint);
    BookingViewModel model = datasource.loadModel(null);
    assertEquals(6, model.bookings.size());
  }
}