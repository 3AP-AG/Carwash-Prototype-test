package ch.aaap.prototype.platform.login;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Login component: repository for app_user. */
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

  Optional<AppUser> findByUsername(String username);

  /**
   * The user exists and has not expired. Run on every request, so expiry or deletion ends a session
   * at once.
   */
  @Query(
      """
      select count(u) > 0 from AppUser u
      where u.username = :username and (u.expiresAt is null or u.expiresAt > current_timestamp)\
      """)
  boolean isUsable(@Param("username") String username);
}
