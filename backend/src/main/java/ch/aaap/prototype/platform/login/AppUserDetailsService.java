package ch.aaap.prototype.platform.login;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login component: loads app_user for Spring's form login. The hash is in DelegatingPasswordEncoder
 * format ({bcrypt}…); an expired user is an expired account, so the login fails.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

  private final AppUserRepository appUserRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    AppUser user =
        appUserRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("No such user"));
    return User.withUsername(user.getUsername())
        .password(user.getPasswordHash())
        .roles("VISITOR")
        .accountExpired(user.isExpired())
        .build();
  }
}
