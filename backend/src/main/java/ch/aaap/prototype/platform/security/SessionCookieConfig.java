package ch.aaap.prototype.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * The session cookie, set explicitly so it is the same under every servlet container and in tests.
 * Spring Boot 4 no longer applies server.servlet.session.cookie.* to Spring Session's cookie.
 * Secure is always on: browsers accept Secure cookies on http://localhost, so the local profile
 * still works. SameSite=Lax, not Strict: a link to the demo from Slack or email is a cross-site
 * navigation, and under Strict it would arrive without the session and send the visitor back to the
 * entry page. Lax still withholds the cookie from cross-site POSTs; CSRF tokens cover the rest.
 */
@Configuration
public class SessionCookieConfig {

  @Bean
  CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName("SESSION");
    serializer.setUseHttpOnlyCookie(true);
    serializer.setUseSecureCookie(true);
    serializer.setSameSite("Lax");
    serializer.setCookiePath("/");
    return serializer;
  }
}
