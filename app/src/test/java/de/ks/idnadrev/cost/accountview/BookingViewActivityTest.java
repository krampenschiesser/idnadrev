package de.ks.idnadrev.cost.accountview;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.executor.group.LastTextChange;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.util.FXPlatform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(LauncherRunner.class)
public class BookingViewActivityTest extends ActivityTest {
  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return BookingViewActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    Account account = new Account("testAccount");
    em.persist(account);

    for (int i = 0; i < 10; i++) {
      int subAdd = i % 2 == 0 ? 1 : -1;
      int amount = i * 10 * subAdd;
      Booking booking = new Booking(account, amount);
      em.persist(booking);
    }
  }

  @Test
  public void testViewBookings() throws Exception {
    BookingViewController bookingView = activityController.getControllerInstance(BookingViewController.class);
    assertThat(bookingView.account.getValue(), not(isEmptyOrNullString()));
    bookingView.applyLoadingHintAndReload();
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    ObservableList<Booking> items = bookingView.bookingTable.getItems();
    Booking addition = items.get(2);
    Booking deletion = items.get(3);
    assertEquals(10, items);
    TableColumn<Booking, Double> columnAdded = (TableColumn<Booking, Double>) bookingView.bookingTable.getColumns().get(3);
    TableColumn<Booking, Double> columnRemoved = (TableColumn<Booking, Double>) bookingView.bookingTable.getColumns().get(4);
    ObservableValue<Double> call = columnAdded.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(bookingView.bookingTable, columnAdded, addition));
    assertEquals(20D, call.getValue(), 0.1D);
    columnAdded.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(bookingView.bookingTable, columnRemoved, deletion));
    assertEquals(-30D, call.getValue(), 0.1D);

  }

  @Test
  public void testFiltering() throws Exception {
    BookingViewController bookingView = activityController.getControllerInstance(BookingViewController.class);
    FXPlatform.invokeLater(() -> bookingView.amount.setText("-30"));

    Thread.sleep(LastTextChange.WAIT_TIME * 2);
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    assertEquals(1, bookingView.bookingTable.getItems().size());
  }
}