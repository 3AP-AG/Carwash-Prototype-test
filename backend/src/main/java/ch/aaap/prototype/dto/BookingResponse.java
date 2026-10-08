package ch.aaap.prototype.dto;

import ch.aaap.prototype.domain.WashProgramme;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/** One booking, in full. */
public record BookingResponse(
    UUID id,
    WashProgramme washProgramme,
    LocalDate appointmentDate,
    LocalTime appointmentTime,
    String customerName,
    String licencePlate,
    Instant createdAt,
    Instant updatedAt,
    long version) {}
