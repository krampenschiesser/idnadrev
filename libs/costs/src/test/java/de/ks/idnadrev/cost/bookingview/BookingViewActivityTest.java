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

import de.ks.executor.group.LastTextChange;
import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.cost.entity.Account;
import de.ks.idnadrev.cost.entity.Booking;
import de.ks.idnadrev.cost.module.CostModule;
import de.ks.standbein.ActivityTest;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BookingViewActivityTest extends ActivityTest {
  private static final Logger log = LoggerFactory.getLogger(BookingViewActivityTest.class);

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new CostModule(), new IntegrationTestModule());

  @Inject
  PersistentWork persistentWork;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return BookingViewActivity.class;
  }

  @Override
  protected void beforeActivityStart() throws Exception {
    super.beforeActivityStart();

    persistentWork.run(session -> {

      Account account = new Account("testAccount");
      session.persist(account);

      LocalDateTime date = LocalDateTime.now().minusMonths(1);
      for (int i = 0; i < 10; i++) {
        int subAdd = i % 2 == 0 ? 1 : -1;
        int amount = i * 10 * subAdd;
        Booking booking = new Booking(account, new BigDecimal(amount));
        booking.setBookingTime(date);
        date = date.plusDays(1);
        session.persist(booking);
      }
    });
  }

  @Test
  public void testViewBookings() throws Exception {
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    persistentWork.run(session -> {
      List<Booking> bookings = persistentWork.from(Booking.class);
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

    TableView<Booking> bookingTable = bookingView.bookingTableController.bookingTable;
    ObservableList<Booking> items = bookingTable.getItems();
    assertEquals(13, items.size());
    Booking addition = items.get(3);
    Booking deletion = items.get(4);
    TableColumn<Booking, Double> columnAmount = (TableColumn<Booking, Double>) bookingTable.getColumns().get(4);
    ObservableValue<Double> call = columnAmount.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(bookingTable, columnAmount, addition));
    assertEquals(20D, call.getValue(), 0.1D);
    call = columnAmount.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(bookingTable, columnAmount, deletion));
    assertEquals(-30D, call.getValue(), 0.1D);
  }

  @Test
  public void testFiltering() throws Exception {
    BookingViewController bookingView = activityController.getControllerInstance(BookingViewController.class);
    FXPlatform.invokeLater(() -> bookingView.amount.setText("-30"));

    Thread.sleep(LastTextChange.WAIT_TIME * 2);
    activityController.waitForDataSource();
    FXPlatform.waitForFX();

    assertEquals(4, bookingView.getBookingTable().getItems().size());
  }

  @Test
  public void testDeleteBooking() throws Exception {
    BookingViewController bookingView = activityController.getControllerInstance(BookingViewController.class);

    TableView<Booking> bookingTable = bookingView.getBookingTable();
    ObservableList<Booking> items = bookingTable.getItems();
    assertEquals(13, items.size());

    FXPlatform.invokeLater(() -> {
      bookingTable.getSelectionModel().select(1, bookingView.bookingTableController.timeColumn);
      KeyEvent keyEvent = new KeyEvent(bookingTable, bookingTable, new EventType<KeyEvent>("test"), " ", " ", KeyCode.SPACE, false, false, false, false);
      bookingTable.getOnKeyPressed().handle(keyEvent);
    });
    Booking selectedItem = bookingTable.getSelectionModel().getSelectedItem();
    SimpleBooleanProperty property = bookingView.bookingTableController.getMarked().get(selectedItem);
    assertTrue("not marked for delete", property.get());

    FXPlatform.invokeLater(() -> bookingView.onDelete(false));
    activityController.waitForDataSource();
    items = bookingTable.getItems();
    assertEquals(12, items.size());
  }
}