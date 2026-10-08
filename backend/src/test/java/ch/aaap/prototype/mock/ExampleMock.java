package ch.aaap.prototype.mock;

import ch.aaap.prototype.domain.Example;
import ch.aaap.prototype.domain.ExampleStatus;
import ch.aaap.prototype.dto.CreateExampleRequest;
import ch.aaap.prototype.dto.ExampleResponse;
import ch.aaap.prototype.dto.ExampleSummaryResponse;
import ch.aaap.prototype.dto.UpdateExampleRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

/** Test fixtures for Example: one place that builds entities and DTOs for tests. */
public final class ExampleMock {

  public static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

  private ExampleMock() {}

  public static Example anExample(String title) {
    return new Example(title, ExampleStatus.OPEN, "A description.");
  }

  /** An example as loaded from the database: it has a version. */
  public static Example aStoredExample(String title, long version) {
    return withVersion(anExample(title), version);
  }

  /** What saving does to the version (the entity has no setter for it); for mocked repositories. */
  public static Example withVersion(Example example, long version) {
    ReflectionTestUtils.setField(example, "version", version);
    return example;
  }

  public static ExampleSummaryResponse aSummary(String title) {
    return new ExampleSummaryResponse(UUID.randomUUID(), title, ExampleStatus.OPEN, CREATED_AT);
  }

  public static ExampleResponse aResponse(UUID id, String title) {
    return new ExampleResponse(
        id, title, ExampleStatus.OPEN, "A description.", CREATED_AT, CREATED_AT, 0);
  }

  public static CreateExampleRequest aCreateRequest(String title) {
    return new CreateExampleRequest(title, ExampleStatus.OPEN, "A description.");
  }

  public static UpdateExampleRequest anUpdateRequest(
      String title, ExampleStatus status, long version) {
    return new UpdateExampleRequest(title, status, "Changed.", version);
  }
}
