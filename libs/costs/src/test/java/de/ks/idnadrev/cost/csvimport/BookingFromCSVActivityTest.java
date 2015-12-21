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

package de.ks.idnadrev.cost.csvimport;

import de.ks.flatjsondb.PersistentWork;
import de.ks.idnadrev.cost.entity.Account;
import de.ks.idnadrev.cost.entity.Booking;
import de.ks.idnadrev.cost.entity.BookingCsvTemplate;
import de.ks.idnadrev.cost.module.CostModule;
import de.ks.standbein.ActivityTest;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BookingFromCSVActivityTest extends ActivityTest {

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new CostModule(), new IntegrationTestModule());
  @Inject
  PersistentWork persistentWork;

  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return BookingFromCSVActivity.class;
  }

  @Override
  protected void beforeActivityStart() throws Exception {
    Account account1 = new Account("account1");
    Account account2 = new Account("account2");

    BookingCsvTemplate template1 = new BookingCsvTemplate("template1");
    template1.setAccount(account2).setSeparator(";").setDateColumn(0).setTimeColumn(1).setDescriptionColumn(4).setAmountColumns(Arrays.asList(5, 6))//
      .setDatePattern("d.M.y").setTimePattern("H:m");

    BookingCsvTemplate template2 = new BookingCsvTemplate("template2");
    template2.setAccount(account1).setSeparator(",").setDateColumn(0).setTimeColumn(0).setDescriptionColumn(2).setAmountColumns(Arrays.asList(5))//
      .setDatePattern("M/d/y").setTimePattern("M/d/y H:m");

    persistentWork.persist(account1);
    persistentWork.persist(account2);
    persistentWork.persist(template1);
    persistentWork.persist(template2);
  }

  @Test
  public void testImportTemplate() throws Exception {
    BookingFromCSVController controller = activityController.getControllerInstance(BookingFromCSVController.class);
    URL resource = getClass().getResource("test.csv");
    controller.onSelectFile(new File(resource.getFile()));
    activityController.waitForDataSource();

    ObservableList<Booking> items = controller.bookingTableController.getBookingTable().getItems();
    ImporterBookingViewModel model = store.getModel();
    assertEquals(3, items.size());
    assertEquals(0, persistentWork.from(Booking.class).size());

    FXPlatform.invokeLater(() -> controller.bookingTableController.getMarked().values().forEach(v -> v.set(true)));

    controller.onImport();
    activityController.waitForDataSource();

    items = controller.bookingTableController.getBookingTable().getItems();
    assertEquals(0, items.size());
    assertEquals(3, persistentWork.from(Booking.class).size());
  }
}