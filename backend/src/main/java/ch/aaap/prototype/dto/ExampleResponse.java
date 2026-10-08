package ch.aaap.prototype.dto;

import ch.aaap.prototype.domain.ExampleStatus;
import java.time.Instant;
import java.util.UUID;

/**
 * Reference DTO: detail view. Always a record, no Lombok. {@code version} goes back in the update
 * request, so the server can tell whether the edit was based on the current state.
 */
public record ExampleResponse(
    UUID id,
    String title,
    ExampleStatus status,
    String description,
    Instant createdAt,
    Instant updatedAt,
    long version) {}
