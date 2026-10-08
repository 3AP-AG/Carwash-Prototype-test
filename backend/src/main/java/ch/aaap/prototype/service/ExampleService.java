package ch.aaap.prototype.service;

import ch.aaap.prototype.domain.Example;
import ch.aaap.prototype.dto.CreateExampleRequest;
import ch.aaap.prototype.dto.ExampleResponse;
import ch.aaap.prototype.dto.ExampleSummaryResponse;
import ch.aaap.prototype.dto.UpdateExampleRequest;
import ch.aaap.prototype.mapper.ExampleMapper;
import ch.aaap.prototype.platform.error.ConflictException;
import ch.aaap.prototype.platform.error.NotFoundException;
import ch.aaap.prototype.platform.web.PageResponse;
import ch.aaap.prototype.repository.ExampleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reference service: logic and transactions. Throws NotFoundException / ConflictException
 * (platform/error); never builds an HTTP response.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService {

  private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

  private final ExampleRepository exampleRepository;

  @Transactional(readOnly = true)
  public PageResponse<ExampleSummaryResponse> list(int page, int size) {
    return PageResponse.of(
        exampleRepository.findAll(PageRequest.of(page, size, NEWEST_FIRST)),
        ExampleMapper::toSummary);
  }

  @Transactional(readOnly = true)
  public ExampleResponse get(UUID id) {
    return ExampleMapper.toResponse(find(id));
  }

  @Transactional
  public ExampleResponse create(CreateExampleRequest request) {
    Example example = exampleRepository.save(ExampleMapper.fromCreate(request));
    log.info("Created example {}", example.getId());
    return ExampleMapper.toResponse(example);
  }

  @Transactional
  public ExampleResponse update(UUID id, UpdateExampleRequest request) {
    Example example = find(id);
    if (!example.getVersion().equals(request.version())) {
      throw new ConflictException(
          "Example " + id + " was changed by someone else. Reload it and try again.");
    }
    ExampleMapper.applyUpdate(example, request);
    // Flush now: the version check and bump happen here, so the response carries the new version.
    exampleRepository.flush();
    return ExampleMapper.toResponse(example);
  }

  private Example find(UUID id) {
    return exampleRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Example " + id + " does not exist."));
  }
}
