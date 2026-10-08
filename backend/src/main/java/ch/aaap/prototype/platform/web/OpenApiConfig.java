package ch.aaap.prototype.platform.web;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shapes the generated spec (api/openapi.yaml) so the generated client is precise: every DTO
 * property is required (DTOs have no optional fields), and there is no server URL, because frontend
 * and API share one origin. Controllers name their tag after the feature (@Tag(name = "examples")),
 * which becomes the generated file name.
 */
@Configuration
public class OpenApiConfig {

  @Bean
  OpenApiCustomizer prototypeOpenApi() {
    return openApi -> {
      openApi.setInfo(new Info().title("Prototype API").version("1"));
      openApi.setServers(List.of());
      if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
        return;
      }
      for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
          schema.setRequired(new ArrayList<>(properties.keySet()));
        }
      }
    };
  }
}
