package de.ks;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 *
 */
public class JunitMatchers {
  private static final Logger log = LoggerFactory.getLogger(JunitMatchers.class);

  public static boolean withRetry(Callable<Boolean> delegate) {
    int rate = 20;
    int timeout = 5000;
    int count = 0;

    boolean success = false;
    while (count < timeout) {
      try {
        if (delegate.call()) {
          return true;
        }
      } catch (Exception e) {
        log.error("Could not execute {}", delegate, e);
        return false;
      }
    }
    return false;
  }
}