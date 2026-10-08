package ch.aaap.prototype.dto;

import ch.aaap.prototype.domain.ExampleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Reference DTO: edit form. Always a record, no Lombok. Same rules as CreateExampleRequest, plus
 * the {@code version} the edit is based on (from ExampleResponse).
 */
public record UpdateExampleRequest(
    @NotBlank @Size(max = 200) String title,
    @NotNull ExampleStatus status,
    @NotNull @Size(max = 2000) String description,
    @NotNull Long version) {}
