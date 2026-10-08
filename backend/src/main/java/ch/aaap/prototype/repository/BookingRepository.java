package ch.aaap.prototype.repository;

import ch.aaap.prototype.domain.Booking;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Bookings. */
public interface BookingRepository extends JpaRepository<Booking, UUID> {}
