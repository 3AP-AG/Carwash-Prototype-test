package ch.aaap.prototype.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * ONE Postgres container per test run, shared by every Spring test context. Import this
 * configuration; never start a container yourself. Testcontainers removes it when the JVM exits.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainer {

  private static final PostgreSQLContainer CONTAINER =
      new PostgreSQLContainer("postgres:17-alpine");

  static {
    CONTAINER.start();
  }

  @Bean(destroyMethod = "")
  @ServiceConnection
  PostgreSQLContainer postgres() {
    return CONTAINER;
  }
}
