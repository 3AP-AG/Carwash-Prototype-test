package ch.aaap.prototype.platform.passcode;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Passcode component: repository for access_token. */
public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {

  /** A token that exists, is not revoked and has not expired. Used at the gate. */
  @Query(
      """
      select t from AccessToken t
      where t.tokenHash = :tokenHash and t.revokedAt is null and t.expiresAt > current_timestamp\
      """)
  Optional<AccessToken> findUsableByHash(@Param("tokenHash") String tokenHash);

  /** Same check by id. Run on every request, so a revoke or expiry takes effect at once. */
  @Query(
      """
      select count(t) > 0 from AccessToken t
      where t.id = :id and t.revokedAt is null and t.expiresAt > current_timestamp\
      """)
  boolean isUsable(@Param("id") UUID id);
}
