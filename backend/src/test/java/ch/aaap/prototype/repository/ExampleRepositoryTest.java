package ch.aaap.prototype.repository;

import static ch.aaap.prototype.mock.ExampleMock.anExample;
import static org.assertj.core.api.Assertions.assertThat;

import ch.aaap.prototype.domain.Example;
import ch.aaap.prototype.support.PostgresContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** Slice test: JPA against the real migrations on the shared Testcontainers Postgres. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgresContainer.class)
class ExampleRepositoryTest {

  @Autowired private ExampleRepository exampleRepository;

  @Test
  void aNewExampleIsInsertedNotMerged() {
    // given
    Example example = anExample("New");

    // when
    Example saved = exampleRepository.saveAndFlush(example);

    // then: persist keeps the instance (merge would return a copy, after an extra SELECT)
    assertThat(saved).isSameAs(example);
    assertThat(saved.getVersion()).isZero();
  }

  @Test
  void pagesThroughExamples() {
    // given
    exampleRepository.save(anExample("One"));
    exampleRepository.save(anExample("Two"));
    exampleRepository.save(anExample("Three"));

    // when
    Page<Example> page = exampleRepository.findAll(PageRequest.of(0, 2, Sort.by("title")));

    // then
    assertThat(page.getTotalElements()).isEqualTo(3);
    assertThat(page.getContent()).extracting(Example::getTitle).containsExactly("One", "Three");
  }
}
