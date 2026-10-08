package ch.aaap.prototype.service;

import static ch.aaap.prototype.mock.BookingMock.DATE;
import static ch.aaap.prototype.mock.BookingMock.TIME;
import static ch.aaap.prototype.mock.BookingMock.aCreateRequest;
import static ch.aaap.prototype.mock.BookingMock.aStoredBooking;
import static ch.aaap.prototype.mock.BookingMock.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.aaap.prototype.domain.Booking;
import ch.aaap.prototype.domain.WashProgramme;
import ch.aaap.prototype.dto.BookingResponse;
import ch.aaap.prototype.platform.error.NotFoundException;
import ch.aaap.prototype.repository.BookingRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit test: Mockito, no Spring context. */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock private BookingRepository bookingRepository;

  @InjectMocks private BookingService bookingService;

  @Nested
  class CreateBooking {

    @Test
    void savesTheBookingWithTrimmedText() {
      // given
      when(bookingRepository.save(any(Booking.class)))
          .thenAnswer(call -> withVersion(call.getArgument(0), 0));

      // when
      BookingResponse result = bookingService.create(aCreateRequest("  Anna Muster  "));

      // then
      assertThat(result.customerName()).isEqualTo("Anna Muster");
      assertThat(result.washProgramme()).isEqualTo(WashProgramme.COMFORT);
      assertThat(result.appointmentDate()).isEqualTo(DATE);
      assertThat(result.appointmentTime()).isEqualTo(TIME);
      assertThat(result.createdAt()).isEqualTo(result.updatedAt());
      verify(bookingRepository).save(any(Booking.class));
    }
  }

  @Nested
  class GetBooking {

    @Test
    void returnsTheBooking() {
      // given
      Booking booking = aStoredBooking("Anna Muster", 0);
      when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

      // when
      BookingResponse result = bookingService.get(booking.getId());

      // then
      assertThat(result.id()).isEqualTo(booking.getId());
      assertThat(result.customerName()).isEqualTo("Anna Muster");
      assertThat(result.licencePlate()).isEqualTo("ZH 123456");
    }

    @Test
    void throwsNotFoundForAnUnknownId() {
      // given
      UUID id = UUID.randomUUID();
      when(bookingRepository.findById(id)).thenReturn(Optional.empty());

      // when / then
      assertThatThrownBy(() -> bookingService.get(id))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining(id.toString());
    }
  }
}
