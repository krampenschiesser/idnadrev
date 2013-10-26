package de.ks.scheduler;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.validation.Validation;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class CronRangeValidatorTest {
  private Validator validator;

  @Before
  public void setUp() throws Exception {
    validator = Validation.getValidator();
  }

  @Test
  public void testValidation() throws Exception {
    checkNoViolations("0 0 * * *");

    checkViolations("bla 0 1 1 0");
    checkViolations("0 bla 1 1 0");
    checkViolations("0 0 bla 1 0");
    checkViolations("0 0 1 bla 0");
    checkViolations("0 0 1 1 bla");

    checkViolations("60 0 1 1 0");
    checkViolations("80 0 1 1 0");

    checkViolations("0 25 1 1 0");
    checkViolations("0 24 1 1 0");

    checkNoViolations("1 23 1 1 0");

    checkViolations("0 0 32 1 0");
    checkViolations("0 0 0 1 0");
    checkNoViolations("0 0 31 1 0");

    checkViolations("0 0 1 1 7");
    checkNoViolations("0 0 1 1 6");
  }

  private void checkNoViolations(String cron) {
    Crontab crontab = new Crontab(cron);
    Set<ConstraintViolation<Crontab>> violations = validator.validate(crontab);
    assertEquals(0, violations.size());
  }

  private void checkViolations(String cron) {
    Crontab crontab = new Crontab(cron);
    Set<ConstraintViolation<Crontab>> violations = validator.validate(crontab);
    assertTrue(!violations.isEmpty());
    assertEquals(1, violations.size());
  }
}
