package ch.aaap.prototype.platform.passcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * The passcode component end to end through the real security chain, Spring Session JDBC and
 * Postgres: frontend and API both reject requests without a valid, unexpired, unrevoked token.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresContainer.class)
class PasscodeGateTest {

  private static final String VALID = "valid-passcode";
  private static final String EXPIRED = "expired-passcode";
  private static final String REVOKED = "revoked-passcode";
  private static final String API = "/api/examples";

  @Autowired private MockMvc mvc;

  @Autowired private JdbcTemplate jdbc;

  private UUID validId;

  @BeforeEach
  void tokens() {
    jdbc.update("delete from access_token");
    validId = insert(VALID, "now() + interval '1 hour'", "null");
    insert(EXPIRED, "now() - interval '1 minute'", "null");
    insert(REVOKED, "now() + interval '1 hour'", "now()");
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
    void frontendRedirectsToGate() throws Exception {
      mvc.perform(get("/"))
          .andExpect(status().isFound())
          .andExpect(header().string("Location", "/gate"));
      mvc.perform(get("/assets/index.js")).andExpect(status().isFound());
    }

    @Test
    void gatePageIsPublicAndContainsNoAppCode() throws Exception {
      // when
      String html =
          mvc.perform(get("/gate"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      // then
      assertThat(html).contains("name=\"passcode\"").doesNotContain("<script");
    }
  }

  @Nested
  class EnteringAPasscode {

    @Test
    void wrongExpiredAndRevokedPasscodesAreRejected() throws Exception {
      for (String passcode : new String[] {"nonsense", EXPIRED, REVOKED, ""}) {
        mvc.perform(post("/gate").with(csrf()).param("passcode", passcode))
            .andExpect(status().isUnauthorized());
      }
    }

    @Test
    void requiresCsrfToken() throws Exception {
      mvc.perform(post("/gate").param("passcode", VALID)).andExpect(status().isForbidden());
    }

    @Test
    void validPasscodeOpensTheApi() throws Exception {
      // given
      Cookie session = enter(VALID);

      // when / then
      mvc.perform(get(API).cookie(session)).andExpect(status().isOk());
    }

    @Test
    void revokingTheTokenEndsTheSessionOnTheNextRequest() throws Exception {
      // given
      Cookie session = enter(VALID);
      mvc.perform(get(API).cookie(session)).andExpect(status().isOk());

      // when
      jdbc.update("update access_token set revoked_at = now() where id = ?", validId);

      // then
      mvc.perform(get(API).cookie(session)).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class ReturningToTheRequestedPage {

    @Test
    void browserFollowUpRequestsDoNotReplaceTheRequestedPage() throws Exception {
      // given: a deep link, then the browser's own favicon request in the same session
      Cookie session =
          mvc.perform(get("/examples/123").accept(MediaType.TEXT_HTML))
              .andExpect(status().isFound())
              .andReturn()
              .getResponse()
              .getCookie("SESSION");
      assertThat(session).as("session cookie").isNotNull();
      mvc.perform(get("/favicon.ico").accept(MediaType.IMAGE_PNG, MediaType.ALL).cookie(session))
          .andExpect(status().isFound());

      // when
      MvcResult result =
          mvc.perform(post("/gate").with(csrf()).param("passcode", VALID).cookie(session))
              .andExpect(status().isSeeOther())
              .andReturn();

      // then
      assertThat(result.getResponse().getHeader("Location")).contains("/examples/123");
    }
  }

  private Cookie enter(String passcode) throws Exception {
    MvcResult result =
        mvc.perform(post("/gate").with(csrf()).param("passcode", passcode))
            .andExpect(status().isSeeOther())
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

  private UUID insert(String passcode, String expiresAt, String revokedAt) {
    UUID id = UUID.randomUUID();
    jdbc.update(
        "insert into access_token (id, token_hash, label, expires_at, revoked_at) values (?, ?,"
            + " 'test', "
            + expiresAt
            + ", "
            + revokedAt
            + ")",
        id,
        TokenHasher.hash(passcode));
    return id;
  }
}
