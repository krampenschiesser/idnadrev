/*
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
package de.ks.idnadrev.cost.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import de.ks.flatjsondb.RegisteredEntity;
import de.ks.idnadrev.cost.entity.*;

public class CostModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<Class> entities = Multibinder.newSetBinder(binder(), Class.class, RegisteredEntity.class);
    entities.addBinding().toInstance(Account.class);
    entities.addBinding().toInstance(Booking.class);
    entities.addBinding().toInstance(BookingCsvTemplate.class);
    entities.addBinding().toInstance(BookingPattern.class);
    entities.addBinding().toInstance(ScheduledBooking.class);
  }
}
