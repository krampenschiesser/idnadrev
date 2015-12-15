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

package de.ks.idnadrev.cost.createbooking;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class CreateBookingActivityTest extends ActivityTest {

  @Override
  protected void createTestData(EntityManager em) {
    Account testAccount = new Account("testAccount");
    em.persist(testAccount);
  }

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return CreateBookingActivity.class;
  }

  @Test
  public void testGeneral() throws Exception {
    CreateBookingController controller = activityController.getControllerInstance(CreateBookingController.class);
    assertEquals("testAccount", controller.account.getValue());

    FXPlatform.invokeLater(() -> controller.amount.setText("bla"));
    assertTrue(controller.book.isDisabled());

    FXPlatform.invokeLater(() -> controller.amount.setText("123"));
    activityController.waitForTasks();
    FXPlatform.waitForFX();
    assertFalse(controller.book.isDisabled());
  }

  @Test
  public void testBooking() throws Exception {
    CreateBookingController controller = activityController.getControllerInstance(CreateBookingController.class);
    FXPlatform.invokeLater(() -> {
      controller.amount.setText("123");
      controller.category.setText("Steak");
      controller.description.setText("1KG");
      controller.onBooking();
    });
    activityController.waitForDataSource();
    List<Booking> bookings = PersistentWork.from(Booking.class);
    assertEquals(1, bookings.size());
    Booking booking = bookings.get(0);
    assertNotNull(booking.getAccount());
    assertEquals(123, booking.getAmount(), 0.01D);
    assertEquals(LocalDate.now(), booking.getBookingTime().toLocalDate());
    assertEquals("Steak", booking.getCategory());
    assertEquals("1KG", booking.getDescription());
  }
}