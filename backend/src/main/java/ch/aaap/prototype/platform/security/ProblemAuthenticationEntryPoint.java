package ch.aaap.prototype.platform.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Unauthenticated /api/** requests get a 401 ProblemDetail; the frontend then reloads the page, and
 * the chain sends the browser to the entry page (/gate or /login).
 */
class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final String BODY =
      """
      {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Authentication required"}\
      """;

  @Override
  public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.getWriter().write(BODY);
  }
}
