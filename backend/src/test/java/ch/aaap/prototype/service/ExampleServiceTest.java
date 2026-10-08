package ch.aaap.prototype.service;

import static ch.aaap.prototype.mock.ExampleMock.aCreateRequest;
import static ch.aaap.prototype.mock.ExampleMock.aStoredExample;
import static ch.aaap.prototype.mock.ExampleMock.anExample;
import static ch.aaap.prototype.mock.ExampleMock.anUpdateRequest;
import static ch.aaap.prototype.mock.ExampleMock.withVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.aaap.prototype.domain.Example;
import ch.aaap.prototype.domain.ExampleStatus;
import ch.aaap.prototype.dto.ExampleResponse;
import ch.aaap.prototype.dto.ExampleSummaryResponse;
import ch.aaap.prototype.platform.error.ConflictException;
import ch.aaap.prototype.platform.error.NotFoundException;
import ch.aaap.prototype.platform.web.PageResponse;
import ch.aaap.prototype.repository.ExampleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Unit test: Mockito, no Spring context. */
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {

  @Mock private ExampleRepository exampleRepository;

  @InjectMocks private ExampleService exampleService;

  @Nested
  class ListExamples {

    @Test
    void returnsOnePageOfSummaries() {
      // given
      when(exampleRepository.findAll(any(Pageable.class)))
          .thenReturn(new PageImpl<>(List.of(anExample("First")), PageRequest.of(1, 10), 11));

      // when
      PageResponse<ExampleSummaryResponse> result = exampleService.list(1, 10);

      // then
      assertThat(result.items()).extracting(ExampleSummaryResponse::title).containsExactly("First");
      assertThat(result.page()).isEqualTo(1);
      assertThat(result.size()).isEqualTo(10);
      assertThat(result.totalItems()).isEqualTo(11);
    }

    @Test
    void sortsNewestFirst() {
      // given
      when(exampleRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

      // when
      exampleService.list(0, 20);

      // then
      ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
      verify(exampleRepository).findAll(pageable.capture());
      assertThat(pageable.getValue().getSort())
          .isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
  }

  @Nested
  class GetExample {

    @Test
    void returnsTheExample() {
      // given
      Example example = aStoredExample("First", 0);
      when(exampleRepository.findById(example.getId())).thenReturn(Optional.of(example));

      // when
      ExampleResponse result = exampleService.get(example.getId());

      // then
      assertThat(result.id()).isEqualTo(example.getId());
      assertThat(result.title()).isEqualTo("First");
    }

    @Test
    void throwsNotFoundForAnUnknownId() {
      // given
      UUID id = UUID.randomUUID();
      when(exampleRepository.findById(id)).thenReturn(Optional.empty());

      // when / then
      assertThatThrownBy(() -> exampleService.get(id))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining(id.toString());
    }
  }

  @Nested
  class CreateExample {

    @Test
    void savesTheExampleWithTrimmedText() {
      // given
      when(exampleRepository.save(any(Example.class)))
          .thenAnswer(call -> withVersion(call.getArgument(0), 0));

      // when
      ExampleResponse result = exampleService.create(aCreateRequest("  New  "));

      // then
      assertThat(result.title()).isEqualTo("New");
      assertThat(result.status()).isEqualTo(ExampleStatus.OPEN);
      assertThat(result.createdAt()).isEqualTo(result.updatedAt());
      verify(exampleRepository).save(any(Example.class));
    }
  }

  @Nested
  class UpdateExample {

    @Test
    void changesTheFieldsAndUpdatedAt() {
      // given
      Example example = aStoredExample("Old", 3);
      when(exampleRepository.findById(example.getId())).thenReturn(Optional.of(example));

      // when
      ExampleResponse result =
          exampleService.update(example.getId(), anUpdateRequest("New", ExampleStatus.CLOSED, 3));

      // then
      assertThat(result.title()).isEqualTo("New");
      assertThat(result.status()).isEqualTo(ExampleStatus.CLOSED);
      assertThat(result.description()).isEqualTo("Changed.");
      assertThat(result.updatedAt()).isAfterOrEqualTo(result.createdAt());
    }

    @Test
    void throwsNotFoundForAnUnknownId() {
      // given
      UUID id = UUID.randomUUID();
      when(exampleRepository.findById(id)).thenReturn(Optional.empty());

      // when / then
      assertThatThrownBy(
              () -> exampleService.update(id, anUpdateRequest("New", ExampleStatus.OPEN, 0)))
          .isInstanceOf(NotFoundException.class);
    }

    @Test
    void throwsConflictWhenTheEditIsBasedOnAnOlderVersion() {
      // given
      Example example = aStoredExample("Old", 4);
      when(exampleRepository.findById(example.getId())).thenReturn(Optional.of(example));

      // when / then
      assertThatThrownBy(
              () ->
                  exampleService.update(
                      example.getId(), anUpdateRequest("New", ExampleStatus.OPEN, 3)))
          .isInstanceOf(ConflictException.class);
      assertThat(example.getTitle()).isEqualTo("Old");
    }
  }
}
