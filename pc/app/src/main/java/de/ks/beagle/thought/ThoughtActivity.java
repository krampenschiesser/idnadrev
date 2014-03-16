package de.ks.beagle.thought;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.activity.Activity;
import de.ks.activity.ActivityController;
import de.ks.application.Navigator;
import de.ks.menu.MenuItem;

import javax.inject.Inject;

@MenuItem("/main/activity")
public class ThoughtActivity extends Activity {
  @Inject
  public ThoughtActivity(ActivityController activityController, Navigator navigator) {
    super(AddThought.class, activityController, navigator);
    configure();
  }

  private void configure() {
    withTask(getInitialController(), "save", SaveThought.class);
  }
}
