package ch.aaap.prototype;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.aaap.prototype.support.PostgresContainer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Builds the OpenAPI spec from the code on every run. Writes it to api/openapi.yaml only with
 * -Dopenapi.export=true ({@code make generate}, {@code make check}), so a plain test run never
 * changes tracked files. No condition annotation: the test gate allows no skipped tests.
 */
@SpringBootTest(properties = "springdoc.api-docs.enabled=true")
@AutoConfigureMockMvc
@Import(PostgresContainer.class)
class OpenApiSpecExportTest {

  private static final Path SPEC = Path.of("..", "api", "openapi.yaml");

  @Autowired private MockMvc mvc;

  @Test
  @WithMockUser
  void exportSpec() throws Exception {
    String yaml =
        mvc.perform(get("/v3/api-docs.yaml"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(StandardCharsets.UTF_8);
    if (Boolean.getBoolean("openapi.export")) {
      Files.writeString(SPEC, yaml.endsWith("\n") ? yaml : yaml + "\n");
    }
  }
}
