/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@freenet.de]
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

package de.ks.activity.context;


import de.ks.executor.ThreadCallBoundValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedList;

/**
 *
 */
public class ActivityPropagator implements ThreadCallBoundValue {
  private static final Logger log = LoggerFactory.getLogger(ActivityPropagator.class);
  protected final ActivityContext context;
  private String propagatedActivityId;
  private String propagatedActivitySequence;

  @Inject
  public ActivityPropagator(ActivityContext context) {
    this.context = context;
  }

  @Override
  public void initializeInCallerThread() {
    LinkedList<String> activityIds = context.activityStack.get();
    if (!activityIds.isEmpty()) {
      propagatedActivityId = activityIds.getLast();
      propagatedActivitySequence = context.getHolder().getId();
      context.registerPlannedPropagation(propagatedActivityId);
    }
  }

  @Override
  public void doBeforeCallInTargetThread() {
    if (propagatedActivityId != null) {
      log.debug("Propagating activity {} seq={}", propagatedActivityId, propagatedActivitySequence);
      context.propagateActivity(propagatedActivityId);
    } else {
      log.debug("Nothing to propagate seq={}", propagatedActivitySequence);
    }
  }

  @Override
  public void doAfterCallInTargetThread() {
    if (propagatedActivityId != null) {
      log.debug("Stopping activity {}->{}", propagatedActivitySequence, propagatedActivityId);
      context.stopActivity(propagatedActivityId);
    } else {
      log.debug("Nothing to stopActivity seq={}", propagatedActivitySequence);
    }
  }

  public ActivityPropagator clone() {
    try {
      ActivityPropagator clone = (ActivityPropagator) super.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new InternalError("Could not clone " + getClass().getName());
    }
  }
}