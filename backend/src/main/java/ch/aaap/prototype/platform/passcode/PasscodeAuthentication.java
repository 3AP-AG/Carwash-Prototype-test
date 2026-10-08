package ch.aaap.prototype.platform.passcode;

import java.util.List;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** The session's proof that a passcode was entered. Holds the token id, never the token. */
public class PasscodeAuthentication extends AbstractAuthenticationToken {

  private final UUID tokenId;

  public PasscodeAuthentication(UUID tokenId) {
    super(List.of(new SimpleGrantedAuthority("ROLE_VISITOR")));
    this.tokenId = tokenId;
    setAuthenticated(true);
  }

  public UUID tokenId() {
    return tokenId;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return "passcode:" + tokenId;
  }
}
