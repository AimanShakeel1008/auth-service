package com.aiplms.auth.dto.v1;

public final class ValidationMessages {
    private ValidationMessages() {}

    public static final String REQUIRED = "This field is required";
    public static final String INVALID_EMAIL = "Must be a valid email address";
    public static final String USERNAME_SIZE = "Username must be between 3 and 50 characters";
    public static final String PASSWORD_SIZE = "Password must be at least 8 characters and at most 128 characters";
    public static final String TOKEN_REQUIRED = "Token is required";
    public static final String PASSWORD_CONFIRM_REQUIRED = "Password confirmation is required";
    public static final String PASSWORDS_MUST_MATCH = "Passwords must match";
}

