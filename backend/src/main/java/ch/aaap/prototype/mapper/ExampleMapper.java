package ch.aaap.prototype.mapper;

import ch.aaap.prototype.domain.Example;
import ch.aaap.prototype.dto.CreateExampleRequest;
import ch.aaap.prototype.dto.ExampleResponse;
import ch.aaap.prototype.dto.ExampleSummaryResponse;
import ch.aaap.prototype.dto.UpdateExampleRequest;

/** Reference mapper: entity ↔ DTO, in one place. */
public final class ExampleMapper {

  private ExampleMapper() {}

  public static ExampleSummaryResponse toSummary(Example example) {
    return new ExampleSummaryResponse(
        example.getId(), example.getTitle(), example.getStatus(), example.getCreatedAt());
  }

  public static ExampleResponse toResponse(Example example) {
    return new ExampleResponse(
        example.getId(),
        example.getTitle(),
        example.getStatus(),
        example.getDescription(),
        example.getCreatedAt(),
        example.getUpdatedAt(),
        example.getVersion());
  }

  public static Example fromCreate(CreateExampleRequest request) {
    return new Example(request.title().strip(), request.status(), request.description().strip());
  }

  public static void applyUpdate(Example example, UpdateExampleRequest request) {
    example.update(request.title().strip(), request.status(), request.description().strip());
  }
}
