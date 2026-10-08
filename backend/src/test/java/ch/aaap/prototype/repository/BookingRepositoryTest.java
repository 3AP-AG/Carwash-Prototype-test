package ch.aaap.prototype.repository;

import static ch.aaap.prototype.mock.BookingMock.DATE;
import static ch.aaap.prototype.mock.BookingMock.TIME;
import static ch.aaap.prototype.mock.BookingMock.aBooking;
import static org.assertj.core.api.Assertions.assertThat;

import ch.aaap.prototype.domain.Booking;
import ch.aaap.prototype.support.PostgresContainer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

/** Slice test: JPA against the real migrations on the shared Testcontainers Postgres. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresContainer.class)
class BookingRepositoryTest {

  @Autowired private BookingRepository bookingRepository;

  @Test
  void storesTheAppointmentDateAndTime() {
    // given
    Booking booking = aBooking("Anna Muster");

    // when
    Booking saved = bookingRepository.saveAndFlush(booking);
    bookingRepository.flush();
    Optional<Booking> found = bookingRepository.findById(saved.getId());

    // then
    assertThat(saved).isSameAs(booking);
    assertThat(saved.getVersion()).isZero();
    assertThat(found)
        .get()
        .satisfies(
            it -> {
              assertThat(it.getAppointmentDate()).isEqualTo(DATE);
              assertThat(it.getAppointmentTime()).isEqualTo(TIME);
              assertThat(it.getLicencePlate()).isEqualTo("ZH 123456");
            });
  }
}
