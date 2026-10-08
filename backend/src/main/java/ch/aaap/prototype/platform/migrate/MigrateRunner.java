package ch.aaap.prototype.platform.migrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * {@code <image> migrate}: runs Flyway against the prototype's database, then exits 0, or non-zero
 * on failure. Starts only a DataSource and Flyway: no web server, no JPA, no app beans. The
 * pipeline calls it as its own step before the candidate deploy (CAAS-1380); the app itself never
 * migrates at startup.
 */
public final class MigrateRunner {

  public static final String COMMAND = "migrate";

  private MigrateRunner() {}

  public static void run(String[] args) {
    SpringApplication app = new SpringApplication(MigrateConfig.class);
    // application-migrate.yml: no web server, Flyway on.
    app.setAdditionalProfiles("migrate");
    ConfigurableApplicationContext context = app.run(args);
    System.exit(SpringApplication.exit(context));
  }

  @Configuration(proxyBeanMethods = false)
  @ImportAutoConfiguration({DataSourceAutoConfiguration.class, FlywayAutoConfiguration.class})
  static class MigrateConfig {}
}
