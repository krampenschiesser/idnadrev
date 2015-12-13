package de.ks.idnadrev.cost.csvimport;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BookingFromCSVImporterTest {
  private BookingFromCSVImporter importer;
  private static final String simpleAddBooking = "02.16.2015;DE4711;DE0815;Simple adding test createbooking;;31.34;EUR";
  private static final String simpleSubBooking = "03.17.2015;DE4711;;Simple subtracting test createbooking;-7,12;;EUR";

  @Test
  public void testDamnFormatter() throws Exception {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.d.y");

    LocalDate result = LocalDate.parse("02.16.2015", formatter);
    assertEquals(2, result.getMonth().getValue());
    assertEquals(16, result.getDayOfMonth());
    assertEquals(2015, result.getYear());

    try {
      LocalTime time = LocalTime.parse("02.16.2015", formatter);
      fail("No parse exception any more");
    } catch (DateTimeException e) {

    }
  }

  @Test
  public void testSplitLength() throws Exception {
    assertEquals(7, simpleAddBooking.split("\\;").length);
    assertEquals(6, StringUtils.split(simpleAddBooking, ";").length);
    assertEquals(5, StringUtils.split(simpleSubBooking, ";").length);
  }

  @Test
  public void testImportFromCSV() throws Exception {
    BookingColumnMapping<LocalDate> dateMapping = new BookingDateColumnMapping(0, "M.d.y");
    BookingColumnMapping<String> descMapping = new DescriptionColumnMapping(3);
    BookingColumnMapping<Double> value1 = new AmountColumnMapping(4, true);
    BookingColumnMapping<Double> value2 = new AmountColumnMapping(5, false);

    importer = new BookingFromCSVImporter(";", dateMapping, value1, value2, descMapping);

    List<Booking> bookings = importer.createBookings(Arrays.asList(simpleAddBooking, simpleSubBooking));
    assertEquals(2, bookings.size());
    Booking first = bookings.get(0);
    Booking second = bookings.get(1);

    LocalDateTime firstDate = LocalDateTime.of(2015, 2, 16, 0, 0);
    LocalDateTime secondDate = LocalDateTime.of(2015, 3, 17, 0, 0);

    assertEquals(firstDate, first.getBookingTime());
    assertEquals(secondDate, second.getBookingTime());

    assertEquals(31.34, first.getAmount(), 0.0001);
    assertEquals(-7.12, second.getAmount(), 0.0001);
  }

  @Test
  public void testParseBigNumber() throws Exception {
    BookingColumnMapping<Double> value1 = new AmountColumnMapping(0, true);
    Booking booking = new Booking();
    value1.apply(booking, new String[]{"31.389,34"});
    assertEquals(31389.34, booking.getAmount(), 0.001);

    value1 = new AmountColumnMapping(0, false);
    value1.apply(booking, new String[]{"31,389.34"});
    assertEquals(31389.34, booking.getAmount(), 0.001);
  }
}