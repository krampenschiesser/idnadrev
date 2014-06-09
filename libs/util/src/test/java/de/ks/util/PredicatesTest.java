/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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

package de.ks.util;

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
