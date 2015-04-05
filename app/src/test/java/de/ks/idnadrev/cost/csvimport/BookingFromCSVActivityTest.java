package de.ks.idnadrev.cost.csvimport;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.BookingCsvTemplate;
import de.ks.util.FXPlatform;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
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
    template1.setAccount(account2).setSeparator(";").setDateColumn(0).setTimeColumn(1).setDescriptionColumn(4).setAmountColumns(Arrays.asList(5, 6)).setDatePattern("d.M.y").setTimePattern("H:m");

    BookingCsvTemplate template2 = new BookingCsvTemplate("template2");
    template2.setAccount(account1).setSeparator(",").setDateColumn(0).setTimeColumn(0).setDescriptionColumn(2).setAmountColumns(Arrays.asList(5)).setDatePattern("M/d/y");

    em.persist(account1);
    em.persist(account2);
    em.persist(template1);
    em.persist(template2);
  }

  @Test
  public void testTemplateHandling() throws Exception {
    activityController.waitForTasks();
    FXPlatform.waitForFX();

    CSVParseDefinitionController controller = activityController.getControllerInstance(CSVParseDefinitionController.class);
    assertEquals(2, controller.templates.getItems().size());
    assertEquals("template1", controller.templates.getSelectionModel().getSelectedItem().getName());

    assertEquals(2, controller.account.getItems().size());
    assertEquals("account2", controller.account.getSelectionModel().getSelectedItem());//selected from template


    assertEquals(";", controller.separator.getText());
    assertEquals("5,6", controller.amountColumn.getText());
    assertEquals("0", controller.dateColumn.getText());
    assertEquals("1", controller.timeColumn.getText());
    assertEquals("4", controller.descriptionColumn.getText());
    assertEquals("d.M.y", controller.datePattern.getText());
    assertEquals("H:m", controller.timePattern.getText());
  }
}