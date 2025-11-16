package com.aiplms.auth.exception;

import org.springframework.http.HttpStatus;

public final class Exceptions {
    private Exceptions() {}

    public static BaseException userAlreadyExists(String detail) {
        return new BaseException("AUTH_ERR_USER_EXISTS", HttpStatus.CONFLICT, detail);
    }

    public static BaseException unauthorized(String detail) {
        return new BaseException("AUTH_ERR_UNAUTHORIZED", HttpStatus.UNAUTHORIZED, detail);
    }

    public static BaseException badRequest(String detail) {
        return new BaseException("AUTH_ERR_BAD_REQUEST", HttpStatus.BAD_REQUEST, detail);
    }

    public static BaseException internal(String detail) {
        return new BaseException("AUTH_ERR_INTERNAL", HttpStatus.INTERNAL_SERVER_ERROR, detail);
    }
}

