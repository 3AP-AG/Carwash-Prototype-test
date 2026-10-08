package ch.aaap.prototype.platform.passcode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Lowercase hex SHA-256 of the UTF-8 token: the format the platform writes
 * (docs/platform-contract.md §2).
 */
public final class TokenHasher {

  private TokenHasher() {}

  public static String hash(String token) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is always available in the JDK", e);
    }
  }
}
