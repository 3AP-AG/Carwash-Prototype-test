package ch.aaap.prototype.dto;

import ch.aaap.prototype.domain.ExampleStatus;
import java.time.Instant;
import java.util.UUID;

/** Reference DTO: one list row. Always a record, no Lombok. */
public record ExampleSummaryResponse(
    UUID id, String title, ExampleStatus status, Instant createdAt) {}
