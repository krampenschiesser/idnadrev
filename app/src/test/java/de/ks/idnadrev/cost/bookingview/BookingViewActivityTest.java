package de.ks.idnadrev.cost.bookingview;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.executor.group.LastTextChange;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

@RunWith(LauncherRunner.class)
public class BookingViewActivityTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(BookingViewActivityTest.class);

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return BookingViewActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    Account account = new Account("testAccount");
    em.persist(account);

    LocalDateTime date = LocalDateTime.now().minusMonths(1);
    for (int i = 0; i < 10; i++) {
      int subAdd = i % 2 == 0 ? 1 : -1;
      int amount = i * 10 * subAdd;
      Booking booking = new Booking(account, amount);
      booking.setBookingTime(date);
      date = date.plusDays(1);
      em.persist(booking);
    }
  }

  @Test
  public void testViewBookings() throws Exception {
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    PersistentWork.wrap(() -> {
      List<Booking> bookings = PersistentWork.from(Booking.class);
      assertEquals(10, bookings.size());
      Booking first = bookings.get(0);
      assertNotNull(first.getAccount());
      assertNotNull(first.getBookingTime());
    });

    BookingViewController bookingView = activityController.getControllerInstance(BookingViewController.class);
    assertThat(bookingView.account.getValue(), not(isEmptyOrNullString()));
    log.info("Reloading now");
    bookingView.applyLoadingHintAndReload();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    ObservableList<Booking> items = bookingView.bookingTable.getItems();
    assertEquals(12, items.size());
    Booking addition = items.get(3);
    Booking deletion = items.get(4);
    TableColumn<Booking, Double> columnAmount = (TableColumn<Booking, Double>) bookingView.bookingTable.getColumns().get(3);
    ObservableValue<Double> call = columnAmount.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(bookingView.bookingTable, columnAmount, addition));
    assertEquals(20D, call.getValue(), 0.1D);
    call = columnAmount.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(bookingView.bookingTable, columnAmount, deletion));
    assertEquals(-30D, call.getValue(), 0.1D);
  }

  @Test
  public void testFiltering() throws Exception {
    BookingViewController bookingView = activityController.getControllerInstance(BookingViewController.class);
    FXPlatform.invokeLater(() -> bookingView.amount.setText("-30"));

    Thread.sleep(LastTextChange.WAIT_TIME * 2);
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    assertEquals(3, bookingView.bookingTable.getItems().size());
  }
}