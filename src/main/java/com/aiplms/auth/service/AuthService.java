package com.aiplms.auth.service;

import com.aiplms.auth.dto.v1.RegisterRequestDto;

import java.util.Map;

public interface AuthService {
    /**
     * Registers a new user.
     * @param request incoming validated DTO (contains passwordConfirm)
     * @return map with minimal public profile: id, username, email
     */
    Map<String, Object> register(RegisterRequestDto request);
}

