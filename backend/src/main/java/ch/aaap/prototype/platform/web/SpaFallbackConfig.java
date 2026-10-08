package ch.aaap.prototype.platform.web;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the built frontend from classpath:/static and answers deep links (/examples/…) with
 * index.html, so the React router takes over. Unknown /api/** paths stay 404. The security chain
 * runs before this, so none of it is served without a session.
 */
@Configuration
public class SpaFallbackConfig implements WebMvcConfigurer {

  private static final Resource INDEX = new ClassPathResource("static/index.html");

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(
            new PathResourceResolver() {
              @Override
              protected Resource getResource(String path, Resource location) throws IOException {
                Resource resource = location.createRelative(path);
                if (resource.isReadable()) {
                  return resource;
                }
                boolean deepLink = !path.startsWith("api/") && !path.contains(".");
                return deepLink && INDEX.isReadable() ? INDEX : null;
              }
            });
  }
}
