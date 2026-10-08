package ch.aaap.prototype.mapper;

import ch.aaap.prototype.domain.Booking;
import ch.aaap.prototype.dto.BookingResponse;
import ch.aaap.prototype.dto.CreateBookingRequest;

/** Booking entity ↔ DTO, in one place. */
public final class BookingMapper {

  private BookingMapper() {}

  public static BookingResponse toResponse(Booking booking) {
    return new BookingResponse(
        booking.getId(),
        booking.getWashProgramme(),
        booking.getAppointmentDate(),
        booking.getAppointmentTime(),
        booking.getCustomerName(),
        booking.getLicencePlate(),
        booking.getCreatedAt(),
        booking.getUpdatedAt(),
        booking.getVersion());
  }

  public static Booking fromCreate(CreateBookingRequest request) {
    return new Booking(
        request.washProgramme(),
        request.appointmentDate(),
        request.appointmentTime(),
        request.customerName().strip(),
        request.licencePlate().strip());
  }
}
