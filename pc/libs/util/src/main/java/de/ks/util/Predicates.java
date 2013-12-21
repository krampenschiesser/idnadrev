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

  private static <T> boolean check(T... predicates) {
    return predicates != null && predicates.length != 0;
  }
}
