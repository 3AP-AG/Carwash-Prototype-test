package ch.aaap.prototype.platform.passcode;

import ch.aaap.prototype.platform.security.SecurityConfig;
import ch.aaap.prototype.platform.web.EntryPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The passcode gate. Server-rendered and outside the frontend bundle, so nothing of the prototype
 * (code, screens, texts) is served before a valid passcode. Not part of the API spec: a form, not
 * JSON. Only in passcode mode.
 */
@Controller
@ConditionalOnProperty(name = "prototype.protection-mode", havingValue = "passcode")
@RequiredArgsConstructor
public class GateController {

  private final AccessTokenRepository accessTokenRepository;
  private final SecurityContextRepository securityContextRepository;
  private final RequestCache requestCache;

  @GetMapping(path = SecurityConfig.GATE_PATH, produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> show(HttpServletRequest request) {
    return page(HttpStatus.OK, request, null);
  }

  @PostMapping(path = SecurityConfig.GATE_PATH, produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> enter(
      @RequestParam(defaultValue = "") String passcode,
      HttpServletRequest request,
      HttpServletResponse response) {
    Optional<AccessToken> token =
        passcode.isBlank()
            ? Optional.empty()
            : accessTokenRepository.findUsableByHash(TokenHasher.hash(passcode.strip()));
    if (token.isEmpty()) {
      return page(HttpStatus.UNAUTHORIZED, request, "That passcode is not valid.");
    }

    request.getSession(true);
    request.changeSessionId(); // no session fixation
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(new PasscodeAuthentication(token.get().getId()));
    SecurityContextHolder.setContext(context);
    securityContextRepository.saveContext(context, request, response);

    SavedRequest saved = requestCache.getRequest(request, response);
    requestCache.removeRequest(request, response);
    String target = saved != null ? saved.getRedirectUrl() : "/";
    return ResponseEntity.status(HttpStatus.SEE_OTHER).header("Location", target).build();
  }

  private ResponseEntity<String> page(HttpStatus status, HttpServletRequest request, String error) {
    CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    String fields =
        """
        <label for="passcode">Passcode</label>
        <input id="passcode" name="passcode" type="password" autocomplete="off" required autofocus>\
        """;
    String html =
        EntryPage.html(
            "This page is protected", SecurityConfig.GATE_PATH, csrf, fields, "Enter", error);
    return ResponseEntity.status(status)
        .header("Cache-Control", "no-store")
        .contentType(MediaType.TEXT_HTML)
        .body(html);
  }
}
