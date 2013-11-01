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
public class PositivePredicate<T> implements Predicate<T> {
  @Override
  public boolean test(T t) {
    return true;
  }
}
