package ch.aaap.prototype.dto;

import ch.aaap.prototype.domain.ExampleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Reference DTO: create form. Always a record, no Lombok. The Bean Validation annotations become
 * the generated Zod schema, so the frontend validates the same rules before sending.
 */
public record CreateExampleRequest(
    @NotBlank @Size(max = 200) String title,
    @NotNull ExampleStatus status,
    @NotNull @Size(max = 2000) String description) {}
