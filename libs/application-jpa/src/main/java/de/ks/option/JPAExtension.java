/**
 * Copyright [2015] [Christian Loehnert]
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
package de.ks.option;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class JPAExtension implements Extension {
  private static final Logger log = LoggerFactory.getLogger(JPAExtension.class);

  public void onOptionSource(@Observes ProcessAnnotatedType<OptionSource> disc) {
    if (disc.getAnnotatedType().getJavaClass().isAssignableFrom(OptionSource.class)) {
      log.info("found {}", disc);
      if (!disc.getAnnotatedType().getJavaClass().equals(JPAOptionSource.class)) {
        disc.veto();
      }
    }
  }
}
