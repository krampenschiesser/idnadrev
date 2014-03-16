package de.ks.activity;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javax.enterprise.inject.Vetoed;
import javax.inject.Singleton;
import java.util.Deque;
import java.util.LinkedList;

/**
 * used to control different activities and their interaction
 */
@Singleton
@Vetoed
public class ActivityController {
  public static final ActivityController instance = new ActivityController();

  protected final Deque<Activity> activities = new LinkedList<>();

  public void start(Activity activity) {
    activity.start();
    activities.add(activity);
  }

  public void stopCurrentResumeLast() {

  }

  public Activity getCurrentActivity() {
    return activities.getLast();
  }
}
