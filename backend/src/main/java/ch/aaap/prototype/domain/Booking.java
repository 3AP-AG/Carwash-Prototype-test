package ch.aaap.prototype.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One wash appointment a customer booked. */
@Entity
@Table(name = "booking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

  @Id private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "wash_programme", nullable = false)
  private WashProgramme washProgramme;

  @Column(name = "appointment_date", nullable = false)
  private LocalDate appointmentDate;

  @Column(name = "appointment_time", nullable = false)
  private LocalTime appointmentTime;

  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @Column(name = "licence_plate", nullable = false)
  private String licencePlate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private Long version;

  public Booking(
      WashProgramme washProgramme,
      LocalDate appointmentDate,
      LocalTime appointmentTime,
      String customerName,
      String licencePlate) {
    this.id = UUID.randomUUID();
    this.washProgramme = washProgramme;
    this.appointmentDate = appointmentDate;
    this.appointmentTime = appointmentTime;
    this.customerName = customerName;
    this.licencePlate = licencePlate;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }
}
