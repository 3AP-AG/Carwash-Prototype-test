package ch.aaap.prototype.platform.login;

import ch.aaap.prototype.platform.security.SecurityConfig;
import ch.aaap.prototype.platform.web.EntryPage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The login page. Server-rendered and outside the frontend bundle, like the passcode gate, so
 * nothing of the prototype is served before a login. Spring's form login handles POST /login. Only
 * in login mode.
 */
@Controller
@ConditionalOnProperty(name = "prototype.protection-mode", havingValue = "login")
public class LoginController {

  @GetMapping(path = SecurityConfig.LOGIN_PATH, produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> show(HttpServletRequest request) {
    boolean failed = request.getParameter("error") != null;
    CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    String error = failed ? "The username or password is not valid." : null;
    String fields =
        """
        <label for="username">Username</label>
        <input id="username" name="username" autocomplete="username" required autofocus>
        <label for="password">Password</label>
        <input id="password" name="password" type="password" autocomplete="current-password" required>\
        """;
    String html =
        EntryPage.html("Sign in", SecurityConfig.LOGIN_PATH, csrf, fields, "Sign in", error);
    return ResponseEntity.status(failed ? HttpStatus.UNAUTHORIZED : HttpStatus.OK)
        .header("Cache-Control", "no-store")
        .contentType(MediaType.TEXT_HTML)
        .body(html);
  }
}
