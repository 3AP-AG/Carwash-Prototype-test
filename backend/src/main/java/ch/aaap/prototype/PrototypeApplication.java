package ch.aaap.prototype;

import ch.aaap.prototype.platform.migrate.MigrateRunner;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Spring Boot entry point. `migrate` as the first argument runs Flyway and exits instead. */
@SpringBootApplication
public class PrototypeApplication {

  public static void main(String[] args) {
    if (args.length > 0 && args[0].equals(MigrateRunner.COMMAND)) {
      MigrateRunner.run(Arrays.copyOfRange(args, 1, args.length));
      return;
    }
    SpringApplication.run(PrototypeApplication.class, args);
  }
}
