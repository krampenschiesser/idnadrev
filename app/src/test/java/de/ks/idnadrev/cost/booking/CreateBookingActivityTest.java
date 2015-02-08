package de.ks.idnadrev.cost.booking;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
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