package com.aiplms.auth.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final OffsetDateTime timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final String supportId;
}


