package com.aiplms.auth.service;

import com.aiplms.auth.entity.User;

public interface EmailVerificationService {

    /**
     * Create verification token for user and send email with verification link.
     * Returns the plain token (for tests) — production callers should not log it.
     */
    String createAndSendToken(User user);

    /**
     * Verify a token (plain token provided by user).
     *
     * @return true if token valid and user activated, false otherwise
     */
    boolean verifyToken(String token);
}
