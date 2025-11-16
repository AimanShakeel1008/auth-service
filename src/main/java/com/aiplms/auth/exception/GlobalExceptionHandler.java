package com.aiplms.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MDC_SUPPORT_ID = "supportId";

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public ErrorResponse handleBaseException(BaseException ex, HttpServletRequest request) {
        // generate supportId for correlation (helpful when user reports an error)
        String supportId = UUID.randomUUID().toString();
        // log exception at WARN/INFO depending on status
        log.warn("Handled BaseException supportId={} status={} errorCode={} message={}",
                supportId, ex.getHttpStatus().value(), ex.getErrorCode(), ex.getMessage());

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(ex.getHttpStatus().value())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .supportId(supportId)
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        String message = String.join(", ", fieldErrors);

        String supportId = UUID.randomUUID().toString();
        log.info("Validation failed supportId={} path={} errors={}", supportId, request.getRequestURI(), message);

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("AUTH_ERR_VALIDATION")
                .message(message)
                .path(request.getRequestURI())
                .supportId(supportId)
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining(", "));

        String supportId = UUID.randomUUID().toString();
        log.info("Constraint violation supportId={} path={} message={}", supportId, request.getRequestURI(), message);

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("AUTH_ERR_VALIDATION")
                .message(message)
                .path(request.getRequestURI())
                .supportId(supportId)
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String supportId = UUID.randomUUID().toString();
        log.info("Missing parameter supportId={} param={} path={}", supportId, ex.getParameterName(), request.getRequestURI());

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("AUTH_ERR_MISSING_PARAM")
                .message(ex.getParameterName() + " parameter is missing")
                .path(request.getRequestURI())
                .supportId(supportId)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String supportId = UUID.randomUUID().toString();
        log.info("Malformed JSON supportId={} path={} cause={}", supportId, request.getRequestURI(), ex.getMessage());

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("AUTH_ERR_MALFORMED_JSON")
                .message("Malformed JSON request")
                .path(request.getRequestURI())
                .supportId(supportId)
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnhandled(Exception ex, HttpServletRequest request) {
        // generate a support id that the client can report back
        String supportId = UUID.randomUUID().toString();

        // put supportId in MDC so subsequent logs include this id (helpful for distributed tracing/log correlation)
        try {
            MDC.put(MDC_SUPPORT_ID, supportId);
            log.error("Unhandled exception supportId={} path={}", supportId, request.getRequestURI(), ex);
        } finally {
            MDC.remove(MDC_SUPPORT_ID);
        }

        return ErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorCode("AUTH_ERR_INTERNAL")
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .supportId(supportId)
                .build();
    }
}
