package com.aiplms.auth.util;

import com.aiplms.auth.dto.v1.ApiResponse;

public final class ResponseUtil {
    private ResponseUtil() {}

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public static <T> ApiResponse<T> success(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

