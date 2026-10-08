package ch.aaap.prototype.platform.security;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Bound from PROTECTION_MODE (application.yml). Missing or invalid stops the app at startup: there
 * is no unprotected mode.
 */
@Validated
@ConfigurationProperties("prototype")
public record ProtectionProperties(@NotNull ProtectionMode protectionMode) {}
