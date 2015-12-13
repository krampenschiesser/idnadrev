/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.cost.csvimport;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BookingFromCSVDS implements DataSource<ImporterBookingViewModel> {
  private static final Logger log = LoggerFactory.getLogger(BookingFromCSVDS.class);
  private static final String KEY_DESCRIPTION = PropertyPath.property(Booking.class, b -> b.getDescription());
  private static final String KEY_BOOKINGTIME = PropertyPath.property(Booking.class, b -> b.getBookingTime());
  private static final String KEY_AMOUNT = PropertyPath.property(Booking.class, b -> b.getAmount());

  private File fileToLoad;
  @Inject
  ActivityController activityController;
  @Inject
  BookingPatternParser patternParser;

  @Override
  public ImporterBookingViewModel loadModel(Consumer<ImporterBookingViewModel> furtherProcessing) {
    ImporterBookingViewModel retval = new ImporterBookingViewModel();

    CSVParseDefinitionController controller = activityController.getControllerInstance(CSVParseDefinitionController.class);
    String accountName = controller.account.getSelectionModel().getSelectedItem();
    if (fileToLoad != null && accountName != null) {
      List<Booking> bookings = Collections.synchronizedList(new LinkedList<>());
      LinkedList<CompletableFuture<Void>> futures = new LinkedList<>();

      Account account = PersistentWork.forName(Account.class, accountName);

      BookingFromCSVImporter importer = controller.getImporter();
      try {
        List<String> lines = Files.readAllLines(fileToLoad.toPath(), Charsets.ISO_8859_1);
        for (String line : lines) {
          try {
            Booking booking = importer.createBooking(line);
            booking.setAccount(account);
            CompletableFuture<Void> future = checkForExistingBooking(bookings, booking, retval);
            futures.add(future);
          } catch (Exception e) {
            log.debug("Error during parsing line \"{}\"", line, e);
            retval.addError(e, line);
          }
        }
      } catch (IOException e) {
        retval.addError(e);
      }
      futures.forEach(f -> f.join());
      retval.getBookings().addAll(bookings);
    }
    return retval;
  }

  protected CompletableFuture<Void> checkForExistingBooking(List<Booking> bookings, Booking booking, ImporterBookingViewModel retval) {
    CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> booking, activityController.getExecutorService()).thenApply(b -> {
      List<Booking> found = PersistentWork.from(Booking.class, (root, query, builder) -> {
        Predicate sameDesc = builder.equal(root.get(KEY_DESCRIPTION), b.getDescription());
        Predicate sameTime = builder.equal(root.get(KEY_BOOKINGTIME), b.getBookingTime());
        Predicate sameAmount = builder.equal(root.get(KEY_AMOUNT), b.getAmount());
        query.where(sameAmount, sameDesc, sameTime);
      }, null);

      if (found.isEmpty()) {
        if (b.getCategory() == null || b.getCategory().isEmpty()) {
          b.setCategory(patternParser.parseLine(b.getDescription()));
        }
        return b;
      } else {
        retval.addError("Booking " + booking.getDescription() + " already exists.");
        return null;
      }
    }).thenAccept(b -> {
      if (b != null) {
        bookings.add(b);
      }
    });
    return future;
  }

  @Override
  public void saveModel(ImporterBookingViewModel model, Consumer<ImporterBookingViewModel> beforeSaving) {
    beforeSaving.accept(model);
    PersistentWork.run(em -> {
      List<Booking> bookingsToImport = model.getBookingsToImport();
      for (Booking booking : bookingsToImport) {
        Account account = PersistentWork.reload(booking.getAccount());
        booking.setAccount(account);
        em.persist(booking);
      }
    });
  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof File) {
      this.fileToLoad = (File) dataSourceHint;
    }
  }
}
