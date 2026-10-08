package ch.aaap.prototype.platform.passcode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Re-checks the session's token on every request: a revoked, expired or deleted token ends the
 * session at once. Created by SecurityConfig inside the chain; not a bean, so it never runs outside
 * it.
 */
@RequiredArgsConstructor
public class PasscodeAuthFilter extends OncePerRequestFilter {

  private final AccessTokenRepository accessTokenRepository;
  private final SecurityContextRepository securityContextRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof PasscodeAuthentication passcode
        && !accessTokenRepository.isUsable(passcode.tokenId())) {
      SecurityContextHolder.clearContext();
      securityContextRepository.saveContext(new SecurityContextImpl(), request, response);
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
    }
    chain.doFilter(request, response);
  }
}
