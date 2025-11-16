package com.aiplms.auth.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public BaseException(String errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

