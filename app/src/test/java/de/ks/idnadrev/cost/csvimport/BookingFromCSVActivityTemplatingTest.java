package de.ks.idnadrev.cost.csvimport;

import de.ks.Condition;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.cost.Account;
import de.ks.idnadrev.entity.cost.BookingCsvTemplate;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(LauncherRunner.class)
public class BookingFromCSVActivityTemplatingTest extends ActivityTest {
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
    template2.setAccount(account1).setSeparator(",").setDateColumn(0).setTimeColumn(0).setDescriptionColumn(2).setAmountColumns(Arrays.asList(5)).setDatePattern("M/d/y").setTimePattern("M/d/y H:m");

    em.persist(account1);
    em.persist(account2);
    em.persist(template1);
    em.persist(template2);
  }

  @Test
  public void testTemplateHandling() throws Exception {
    waitForTemplateLoading();

    CSVParseDefinitionController controller = activityController.getControllerInstance(CSVParseDefinitionController.class);
    assertEquals(2, controller.templates.getItems().size());
    assertEquals("template1", controller.templates.getSelectionModel().getSelectedItem().getName());

    Condition.waitFor1s(() -> controller.account.getItems(), Matchers.hasSize(2));
    assertEquals("account2", controller.account.getSelectionModel().getSelectedItem());//selected from template

    assertEquals(";", controller.separator.getText());
    assertEquals("5,6", controller.amountColumn.getText());
    assertEquals("0", controller.dateColumn.getText());
    assertEquals("1", controller.timeColumn.getText());
    assertEquals("4", controller.descriptionColumn.getText());
    assertEquals("d.M.y", controller.datePattern.getText());
    assertEquals("H:m", controller.timePattern.getText());

    FXPlatform.invokeLater(() -> controller.templates.getSelectionModel().select(1));

    assertEquals("account1", controller.account.getSelectionModel().getSelectedItem());
    assertEquals(",", controller.separator.getText());
    assertEquals("5", controller.amountColumn.getText());
    assertEquals("0", controller.dateColumn.getText());
    assertEquals("0", controller.timeColumn.getText());
    assertEquals("2", controller.descriptionColumn.getText());
    assertEquals("M/d/y", controller.datePattern.getText());
    assertEquals("M/d/y H:m", controller.timePattern.getText());
  }

  @Test
  public void testSaveTemplate() throws Exception {
    waitForTemplateLoading();

    CSVParseDefinitionController controller = activityController.getControllerInstance(CSVParseDefinitionController.class);

    FXPlatform.invokeLater(() -> {
      controller.separator.setText(".");
      controller.amountColumn.setText("4,6");
      controller.timeColumn.setText("2");
      controller.dateColumn.setText("3");
      controller.descriptionColumn.setText("1");
      controller.timePattern.setText("y.M.d");
      controller.datePattern.setText("y.M.d H:m");
      controller.onSaveTemplate("template3");
    });

    List<BookingCsvTemplate> templates = PersistentWork.from(BookingCsvTemplate.class);
    assertEquals(3, templates.size());

    BookingCsvTemplate template = templates.stream().filter(t -> t.getName().endsWith("3")).findFirst().get();
    assertEquals(".", template.getSeparator());
    List<Integer> amountColumns = template.getAmountColumns();
    assertThat(amountColumns, Matchers.containsInAnyOrder(4, 6));
    assertEquals(2, (int) template.getTimeColumn());
    assertEquals(3, template.getDateColumn());
    assertEquals(1, template.getDescriptionColumn());
    assertEquals("y.M.d", template.getTimePattern());
    assertEquals("y.M.d H:m", template.getDatePattern());
  }

  protected void waitForTemplateLoading() {
    activityController.waitForTasks();
    FXPlatform.waitForFX();
  }
}