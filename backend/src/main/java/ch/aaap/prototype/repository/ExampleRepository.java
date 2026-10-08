package ch.aaap.prototype.repository;

import ch.aaap.prototype.domain.Example;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Reference repository: Spring Data JPA. */
public interface ExampleRepository extends JpaRepository<Example, UUID> {}
