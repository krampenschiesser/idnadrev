package de.ks.idnadrev.cost.csvimport;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.idnadrev.entity.cost.BookingCsvTemplate;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(LauncherRunner.class)
public class BookingFromCSVActivityTest extends ActivityTest {
  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return BookingFromCSVActivity.class;
  }

  @Override
  protected void createTestData(EntityManager em) {
    Account account1 = new Account("account1");
    Account account2 = new Account("account2");

    BookingCsvTemplate template1 = new BookingCsvTemplate("template1");
    template1.setAccount(account2).setSeparator(";").setDateColumn(0).setTimeColumn(1).setDescriptionColumn(4).setAmountColumns(Arrays.asList(5, 6))//
      .setDatePattern("d.M.y").setTimePattern("H:m");

    BookingCsvTemplate template2 = new BookingCsvTemplate("template2");
    template2.setAccount(account1).setSeparator(",").setDateColumn(0).setTimeColumn(0).setDescriptionColumn(2).setAmountColumns(Arrays.asList(5))//
      .setDatePattern("M/d/y").setTimePattern("M/d/y H:m");

    em.persist(account1);
    em.persist(account2);
    em.persist(template1);
    em.persist(template2);
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
    assertEquals(0, PersistentWork.from(Booking.class).size());

    FXPlatform.invokeLater(() -> controller.bookingTableController.getMarked().values().forEach(v -> v.set(true)));

    controller.onImport();
    activityController.waitForDataSource();

    items = controller.bookingTableController.getBookingTable().getItems();
    assertEquals(0, items.size());
    assertEquals(3, PersistentWork.from(Booking.class).size());
  }
}