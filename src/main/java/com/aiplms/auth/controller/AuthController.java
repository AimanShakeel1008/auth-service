package com.aiplms.auth.controller;

import com.aiplms.auth.dto.v1.ApiResponse;
import com.aiplms.auth.dto.v1.LoginRequestDto;
import com.aiplms.auth.dto.v1.RegisterRequestDto;
import com.aiplms.auth.dto.v1.UserResponseDto;
import com.aiplms.auth.entity.User;
import com.aiplms.auth.exception.Exceptions;
import com.aiplms.auth.mapper.UserMapper;
import com.aiplms.auth.repository.UserRepository;
import com.aiplms.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequestDto request) {
        Map<String, Object> data = authService.register(request);
        ApiResponse<Map<String, Object>> body = new ApiResponse<>("AUTH_001", "User registered successfully", data);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequestDto request) {
        Map<String, Object> data = authService.login(request);
        ApiResponse<Map<String, Object>> body = new ApiResponse<>("AUTH_002", "Login successful", data);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw Exceptions.unauthorized("Authentication required");
        }

        String principalName = auth.getName();

        // Resolve user by username first, then by email
        var userOpt = userRepository.findByUsernameIgnoreCase(principalName);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmailIgnoreCase(principalName);
        }

        var user = userOpt.orElseThrow(() -> Exceptions.unauthorized("Authenticated user not found"));

        var dto = userMapper.toUserResponse(user);

        var body = new ApiResponse<>("AUTH_011", "Current user retrieved", dto);
        return ResponseEntity.ok(body);
    }


}

