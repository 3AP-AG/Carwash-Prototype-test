package ch.aaap.prototype.platform.security;

import ch.aaap.prototype.platform.login.AppUserRepository;
import ch.aaap.prototype.platform.login.LoginSessionFilter;
import ch.aaap.prototype.platform.passcode.AccessTokenRepository;
import ch.aaap.prototype.platform.passcode.PasscodeAuthFilter;
import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * The one security chain. It covers everything: the frontend (index.html, /assets/**), the API
 * (/api/**) and every other path. The only public path is the entry page of the protection mode:
 * /gate (passcode) or /login (login), both server-rendered outside the frontend bundle.
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ProtectionProperties.class)
public class SecurityConfig {

  public static final String GATE_PATH = "/gate";
  public static final String LOGIN_PATH = "/login";

  private static final RequestMatcher API = PathPatternRequestMatcher.pathPattern("/api/**");

  @Bean
  SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  RequestCache requestCache() {
    // Remember where an unauthenticated visitor wanted to go, so the entry page can send them back.
    // Only page navigations (GET, Accept: text/html, not the API): the browser's own follow-up
    // requests (/favicon.ico, assets) must not overwrite the page the visitor asked for.
    MediaTypeRequestMatcher html = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
    html.setIgnoredMediaTypes(Set.of(MediaType.ALL));
    HttpSessionRequestCache cache = new HttpSessionRequestCache();
    cache.setRequestMatcher(
        new AndRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/**"),
            new NegatedRequestMatcher(API),
            html));
    return cache;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    // Reads the {bcrypt}… format the platform writes into app_user (docs/platform-contract.md §2).
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      ProtectionProperties protection,
      ObjectProvider<AccessTokenRepository> accessTokenRepository,
      ObjectProvider<AppUserRepository> appUserRepository,
      SecurityContextRepository securityContextRepository,
      RequestCache requestCache)
      throws Exception {
    String entryPage = protection.protectionMode() == ProtectionMode.LOGIN ? LOGIN_PATH : GATE_PATH;
    http.securityContext(c -> c.securityContextRepository(securityContextRepository))
        .requestCache(c -> c.requestCache(requestCache))
        .authorizeHttpRequests(
            a -> a.requestMatchers(entryPage).permitAll().anyRequest().authenticated())
        .exceptionHandling(
            e ->
                e.defaultAuthenticationEntryPointFor(new ProblemAuthenticationEntryPoint(), API)
                    .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint(entryPage), request -> true))
        .csrf(c -> c.spa());

    switch (protection.protectionMode()) {
      case PASSCODE ->
          http.addFilterAfter(
              new PasscodeAuthFilter(accessTokenRepository.getObject(), securityContextRepository),
              SecurityContextHolderFilter.class);
      case LOGIN ->
          http.formLogin(f -> f.loginPage(LOGIN_PATH).failureUrl(LOGIN_PATH + "?error").permitAll())
              .addFilterAfter(
                  new LoginSessionFilter(appUserRepository.getObject(), securityContextRepository),
                  SecurityContextHolderFilter.class);
    }
    return http.build();
  }
}
