package com.aiplms.auth.service;

import com.aiplms.auth.dto.v1.LoginRequestDto;
import com.aiplms.auth.dto.v1.RegisterRequestDto;

import java.util.Map;

public interface AuthService {
    /**
     * Registers a new user.
     * @param request incoming validated DTO (contains passwordConfirm)
     * @return map with minimal public profile: id, username, email
     */
    Map<String, Object> register(RegisterRequestDto request);

    /**
     * Authenticate user credentials (username/email + password).
     * Note: JWT generation is performed in later steps.
     * @param request login DTO
     * @return minimal public profile map with id, username, email
     */
    Map<String, Object> login(LoginRequestDto request);

}

