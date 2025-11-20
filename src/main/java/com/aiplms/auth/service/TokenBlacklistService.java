package com.aiplms.auth.service;

public interface TokenBlacklistService {

    /**
     * Blacklist access token string (store an entry in Redis).
     * If the token contains a jti, the jti will be used as key, otherwise fallback to sha256(token).
     *
     * @param jwt the raw JWT string (must be non-null)
     * @throws IllegalArgumentException if jwt is null/blank
     */
    void blacklistAccessToken(String jwt);

    /**
     * Check whether a token is blacklisted.
     *
     * @param jwt the raw JWT string
     * @return true if blacklisted
     */
    boolean isBlacklisted(String jwt);
}
