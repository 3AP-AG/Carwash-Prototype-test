package ch.aaap.prototype.controller;

import ch.aaap.prototype.dto.CreateExampleRequest;
import ch.aaap.prototype.dto.ExampleResponse;
import ch.aaap.prototype.dto.ExampleSummaryResponse;
import ch.aaap.prototype.dto.UpdateExampleRequest;
import ch.aaap.prototype.platform.web.PageResponse;
import ch.aaap.prototype.service.ExampleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reference controller: HTTP only. Maps requests to ExampleService and returns DTOs; never builds
 * error responses. Method names become operationIds, and so the generated hooks (listExamples →
 * useListExamples).
 */
@RestController
@RequestMapping(path = "/api/examples", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "examples")
@RequiredArgsConstructor
public class ExampleController {

  private final ExampleService exampleService;

  @GetMapping
  public PageResponse<ExampleSummaryResponse> listExamples(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return exampleService.list(page, size);
  }

  @GetMapping("/{id}")
  public ExampleResponse getExample(@PathVariable UUID id) {
    return exampleService.get(id);
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ExampleResponse createExample(@Valid @RequestBody CreateExampleRequest request) {
    return exampleService.create(request);
  }

  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ExampleResponse updateExample(
      @PathVariable UUID id, @Valid @RequestBody UpdateExampleRequest request) {
    return exampleService.update(id, request);
  }
}
