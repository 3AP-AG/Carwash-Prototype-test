package ch.aaap.prototype.dto;

import ch.aaap.prototype.domain.WashProgramme;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The booking form a customer fills in. The Bean Validation annotations become the generated Zod
 * schema, so the frontend validates the same rules before sending.
 */
public record CreateBookingRequest(
    @NotNull WashProgramme washProgramme,
    @NotNull LocalDate appointmentDate,
    @NotNull LocalTime appointmentTime,
    @NotBlank(message = "Bitte geben Sie Ihren Namen ein.") @Size(max = 100) String customerName,
    @NotBlank(message = "Bitte geben Sie Ihr Autokennzeichen ein.") @Size(max = 20)
        String licencePlate) {}
