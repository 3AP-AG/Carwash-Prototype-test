package ch.aaap.prototype.platform.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.aaap.prototype.support.PostgresContainer;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * The login component end to end through the real security chain, Spring Session JDBC and Postgres:
 * frontend and API both reject requests without a valid, unexpired user.
 */
@SpringBootTest(properties = "prototype.protection-mode=login")
@AutoConfigureMockMvc
@Import(PostgresContainer.class)
class LoginTest {

  private static final String PASSWORD = "correct-password";
  private static final String API = "/api/examples";

  @Autowired private MockMvc mvc;

  @Autowired private JdbcTemplate jdbc;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void users() {
    jdbc.update("delete from app_user");
    insert("valid", "null");
    insert("expired", "now() - interval '1 minute'");
    insert("run-scoped", "now() + interval '15 minutes'");
  }

  @Nested
  class WithoutSession {

    @Test
    void apiIs401Problem() throws Exception {
      mvc.perform(get(API))
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void frontendRedirectsToLogin() throws Exception {
      mvc.perform(get("/"))
          .andExpect(status().isFound())
          .andExpect(header().string("Location", "/login"));
    }

    @Test
    void loginPageIsPublicAndContainsNoAppCode() throws Exception {
      // when
      String html =
          mvc.perform(get("/login"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      // then
      assertThat(html).contains("name=\"username\"", "name=\"password\"").doesNotContain("<script");
    }

    @Test
    void theGateDoesNotExistInLoginMode() throws Exception {
      mvc.perform(get("/gate")).andExpect(status().isFound());
    }
  }

  @Nested
  class SigningIn {

    @Test
    void wrongPasswordUnknownAndExpiredUsersAreRejected() throws Exception {
      String[][] attempts = {{"valid", "wrong"}, {"nobody", PASSWORD}, {"expired", PASSWORD}};
      for (String[] attempt : attempts) {
        mvc.perform(
                post("/login")
                    .with(csrf())
                    .param("username", attempt[0])
                    .param("password", attempt[1]))
            .andExpect(redirectedUrl("/login?error"));
      }
    }

    @Test
    void requiresCsrfToken() throws Exception {
      mvc.perform(post("/login").param("username", "valid").param("password", PASSWORD))
          .andExpect(status().isForbidden());
    }

    @Test
    void validUserOpensTheApi() throws Exception {
      // given
      Cookie session = signIn("valid");

      // when / then
      mvc.perform(get(API).cookie(session)).andExpect(status().isOk());
    }

    @Test
    void expiringTheUserEndsTheSessionOnTheNextRequest() throws Exception {
      // given
      Cookie session = signIn("run-scoped");
      mvc.perform(get(API).cookie(session)).andExpect(status().isOk());

      // when
      jdbc.update(
          "update app_user set expires_at = now() - interval '1 second' where username = ?",
          "run-scoped");

      // then
      mvc.perform(get(API).cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void deletingTheUserEndsTheSessionOnTheNextRequest() throws Exception {
      // given
      Cookie session = signIn("valid");

      // when
      jdbc.update("delete from app_user where username = ?", "valid");

      // then
      mvc.perform(get(API).cookie(session)).andExpect(status().isUnauthorized());
    }
  }

  private Cookie signIn(String username) throws Exception {
    MvcResult result =
        mvc.perform(
                post("/login").with(csrf()).param("username", username).param("password", PASSWORD))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/"))
            .andReturn();
    String setCookie =
        result.getResponse().getHeaders("Set-Cookie").stream()
            .filter(h -> h.startsWith("SESSION="))
            .findFirst()
            .orElseThrow();
    assertThat(setCookie).contains("HttpOnly", "Secure", "SameSite=Lax");
    Cookie session = result.getResponse().getCookie("SESSION");
    assertThat(session).as("session cookie").isNotNull();
    return session;
  }

  private void insert(String username, String expiresAt) {
    jdbc.update(
        "insert into app_user (id, username, password_hash, display_name, expires_at) values (?, ?,"
            + " ?, ?, "
            + expiresAt
            + ")",
        UUID.randomUUID(),
        username,
        passwordEncoder.encode(PASSWORD),
        username);
  }
}
