package de.ks.util;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class PredicatesTest {
  @Test
  public void testAndCombination() throws Exception {
    assertTrue(Predicates.combineAnd(new PositivePredicate<Object>(), new PositivePredicate<Object>()).test(new Object()));
    assertFalse(Predicates.combineAnd(new NegativePredicate<Object>(), new PositivePredicate<Object>()).test(new Object()));
    assertFalse(Predicates.combineAnd(new NegativePredicate<Object>(), new NegativePredicate<Object>()).test(new Object()));
  }

  @Test
  public void testOrCombination() throws Exception {
    assertTrue(Predicates.combineOr(new PositivePredicate<Object>(), new PositivePredicate<Object>()).test(new Object()));
    assertTrue(Predicates.combineOr(new NegativePredicate<Object>(), new PositivePredicate<Object>()).test(new Object()));
    assertFalse(Predicates.combineOr(new NegativePredicate<Object>(), new NegativePredicate<Object>()).test(new Object()));
  }
}
