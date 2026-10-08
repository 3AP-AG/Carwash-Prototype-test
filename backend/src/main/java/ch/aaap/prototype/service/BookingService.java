package ch.aaap.prototype.service;

import ch.aaap.prototype.domain.Booking;
import ch.aaap.prototype.dto.BookingResponse;
import ch.aaap.prototype.dto.CreateBookingRequest;
import ch.aaap.prototype.mapper.BookingMapper;
import ch.aaap.prototype.platform.error.NotFoundException;
import ch.aaap.prototype.repository.BookingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Booking logic and transactions. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;

  @Transactional(readOnly = true)
  public BookingResponse get(UUID id) {
    return BookingMapper.toResponse(find(id));
  }

  @Transactional
  public BookingResponse create(CreateBookingRequest request) {
    Booking booking = bookingRepository.save(BookingMapper.fromCreate(request));
    log.info("Created booking {}", booking.getId());
    return BookingMapper.toResponse(booking);
  }

  private Booking find(UUID id) {
    return bookingRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Booking " + id + " does not exist."));
  }
}
