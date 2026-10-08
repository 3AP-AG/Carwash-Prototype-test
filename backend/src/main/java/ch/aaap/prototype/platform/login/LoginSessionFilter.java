package ch.aaap.prototype.platform.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Re-checks the session's user on every request: an expired or deleted user (the pipeline's
 * run-scoped CI user, say) ends the session at once. Created by SecurityConfig inside the chain;
 * not a bean, so it never runs outside it.
 */
@RequiredArgsConstructor
public class LoginSessionFilter extends OncePerRequestFilter {

  private final AppUserRepository appUserRepository;
  private final SecurityContextRepository securityContextRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof UsernamePasswordAuthenticationToken login
        && !appUserRepository.isUsable(login.getName())) {
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
