/*
 * Copyright [2014] [Christian Loehnert]
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

package de.ks.util;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import java.util.function.Predicate;

/**
 *
 */
public class Predicates {
  private Predicates() {
    //
  }

  /**
   * Combines the given predicates via AND conjunction
   *
   * @param predicates
   * @param <T>
   * @return null if predicates are null or empty
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> combineAnd(Predicate<T>... predicates) {
    if (!check(predicates)) {
      return null;
    }
    Predicate<T> combined = new PositivePredicate<>();

    for (Predicate<T> predicate : predicates) {
      combined = combined.and(predicate);
    }
    return combined;
  }

  /**
   * Combines the given predicates via OR conjunction
   *
   * @param predicates
   * @param <T>
   * @return null if predicates are null or empty
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <T> Predicate<T> combineOr(Predicate<T>... predicates) {
    if (!check(predicates)) {
      return null;
    }
    if (predicates.length > 1) {
      Predicate<T> combined = predicates[0];
      for (int i = 1; i < predicates.length; i++) {
        combined = combined.or(predicates[i]);
      }
      return combined;
    } else {
      return predicates[0];
    }
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  private static <T> boolean check(T... predicates) {
    return predicates != null && predicates.length != 0;
  }
}
