package com.aiplms.auth.service;

import com.aiplms.auth.entity.RefreshToken;
import com.aiplms.auth.entity.User;

import java.time.Instant;
import java.util.Optional;

/**
 * Responsible for creating and managing refresh tokens.
 *
 * The service returns the plain opaque token string on creation (for the client).
 * The persisted entity will contain only the hashed token.
 */
public interface RefreshTokenService {

    /**
     * Create and persist a refresh token for the user.
     * @param user the user
     * @param expiresAt expiration instant for the refresh token
     * @return pair: plain opaque token (String) and the persisted RefreshToken entity
     */
    CreateResult createForUser(User user, Instant expiresAt);

    Optional<RefreshToken> findByHash(String tokenHash);

    void revoke(RefreshToken token);

    class CreateResult {
        private final String plainToken;
        private final RefreshToken entity;

        public CreateResult(String plainToken, RefreshToken entity) {
            this.plainToken = plainToken;
            this.entity = entity;
        }

        public String getPlainToken() { return plainToken; }
        public RefreshToken getEntity() { return entity; }
    }
}
