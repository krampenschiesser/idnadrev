/*
 * Copyright [${YEAR}] [Christian Loehnert]
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 */
@ActivityScoped()
public class ActivityScopedBean1 {
  private static final Logger log = LoggerFactory.getLogger(ActivityScopedBean1.class);
  protected String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return getClass().getName();
  }

  @PostConstruct
  public void before() {
    log.debug("PostConstruct {}", ActivityScopedBean1.class.getSimpleName());
  }

  @PreDestroy
  public void after() {
    log.debug("PreDestroy {}", ActivityScopedBean1.class.getSimpleName());
  }
}
