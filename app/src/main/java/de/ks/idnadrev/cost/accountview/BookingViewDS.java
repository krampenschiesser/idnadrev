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
package de.ks.idnadrev.cost.accountview;

import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.cost.Booking;
import de.ks.persistence.PersistentWork;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.function.Consumer;

public class BookingViewDS implements DataSource<BookingViewModel> {
  private BookingLoadingHint loadingHint;

  @Override
  public BookingViewModel loadModel(Consumer<BookingViewModel> furtherProcessing) {
    if (loadingHint == null) {
      return new BookingViewModel();
    } else {
      PersistentWork.read(em -> {
        CriteriaBuilder builder = em.getCriteriaBuilder();

        CriteriaQuery<Double> query = builder.createQuery(Double.class);
        Root<Booking> from = query.from(Booking.class);
        loadingHint.applyFilter(query, builder, from, true);
        return 0D;
      });

      List<Booking> bookings = PersistentWork.from(Booking.class, (root, query, builder) -> {
        loadingHint.applyFilter(query, builder, root, true);
      }, null);
      return new BookingViewModel(0D, bookings);
    }
  }

  @Override
  public void saveModel(BookingViewModel model, Consumer<BookingViewModel> beforeSaving) {

  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof BookingLoadingHint) {
      loadingHint = ((BookingLoadingHint) dataSourceHint);
    } else {
      loadingHint = null;
    }
  }
}
