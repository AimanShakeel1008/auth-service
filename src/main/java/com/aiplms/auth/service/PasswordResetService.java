package com.aiplms.auth.service;

import com.aiplms.auth.entity.User;

/**
 * Password reset flow:
 * - createAndSendToken(email) : generates a token for the user (if exists) and sends an email
 *   - returns the plain token for tests (production callers should not log it)
 * - confirmReset(token, newPassword) : verifies the token, updates the user's password and invalidates token
 */
public interface PasswordResetService {

    /**
     * Create token for given email and send password-reset email.
     * Returns the plain token (for tests/dev). Production callers should not log this.
     */
    String createAndSendToken(String email);

    /**
     * Confirm reset by token (plain token) and new password.
     * Returns true if successful.
     */
    boolean confirmReset(String token, String newPassword);
}
