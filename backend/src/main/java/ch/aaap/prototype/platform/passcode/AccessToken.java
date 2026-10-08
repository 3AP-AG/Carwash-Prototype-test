package ch.aaap.prototype.platform.passcode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A client passcode, stored as a hash. Rows are written by the platform's token jobs
 * (docs/platform-contract.md §2); the app only reads them.
 */
@Entity
@Table(name = "access_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessToken {

  @Id private UUID id;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(nullable = false)
  private String label;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;
}
