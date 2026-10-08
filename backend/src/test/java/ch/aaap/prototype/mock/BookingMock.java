package ch.aaap.prototype.mock;

import ch.aaap.prototype.domain.Booking;
import ch.aaap.prototype.domain.WashProgramme;
import ch.aaap.prototype.dto.BookingResponse;
import ch.aaap.prototype.dto.CreateBookingRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

/** Test fixtures for Booking: one place that builds entities and DTOs for tests. */
public final class BookingMock {

  public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
  public static final LocalDate DATE = LocalDate.of(2026, 11, 3);
  public static final LocalTime TIME = LocalTime.of(9, 30);

  private BookingMock() {}

  public static Booking aBooking(String customerName) {
    return new Booking(WashProgramme.COMFORT, DATE, TIME, customerName, "ZH 123456");
  }

  /** A booking as loaded from the database: it has a version. */
  public static Booking aStoredBooking(String customerName, long version) {
    return withVersion(aBooking(customerName), version);
  }

  /** What saving does to the version (the entity has no setter for it); for mocked repositories. */
  public static Booking withVersion(Booking booking, long version) {
    ReflectionTestUtils.setField(booking, "version", version);
    return booking;
  }

  public static BookingResponse aResponse(UUID id, String customerName) {
    return new BookingResponse(
        id,
        WashProgramme.COMFORT,
        DATE,
        TIME,
        customerName,
        "ZH 123456",
        CREATED_AT,
        CREATED_AT,
        0);
  }

  public static CreateBookingRequest aCreateRequest(String customerName) {
    return new CreateBookingRequest(WashProgramme.COMFORT, DATE, TIME, customerName, "ZH 123456");
  }
}
