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
import de.ks.activity.ActivityController;
import de.ks.datasource.DataSource;
import de.ks.idnadrev.entity.cost.Booking;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class BookingFromCSVDS implements DataSource<ImporterBookingViewModel> {
  private File fileToLoad;
  @Inject
  ActivityController activityController;

  @Override
  public ImporterBookingViewModel loadModel(Consumer<ImporterBookingViewModel> furtherProcessing) {
    ImporterBookingViewModel retval = new ImporterBookingViewModel();

    if (fileToLoad != null) {
      LinkedList<Booking> bookings = new LinkedList<>();

      CSVParseDefinitionController controller = activityController.getControllerInstance(CSVParseDefinitionController.class);
      BookingFromCSVImporter importer = controller.getImporter();
      try {
        List<String> lines = Files.readAllLines(fileToLoad.toPath(), Charsets.ISO_8859_1);
        for (String line : lines) {
          try {
            Booking booking = importer.createBooking(line);
            bookings.add(booking);
          } catch (Exception e) {
            retval.addError(e, line);

          }
        }
      } catch (IOException e) {
        retval.addError(e);
      }
      retval.getBookings().addAll(bookings);
    }
    return retval;
  }

  @Override
  public void saveModel(ImporterBookingViewModel model, Consumer<ImporterBookingViewModel> beforeSaving) {

  }

  @Override
  public void setLoadingHint(Object dataSourceHint) {
    if (dataSourceHint instanceof File) {
      this.fileToLoad = (File) dataSourceHint;
    }
  }
}
