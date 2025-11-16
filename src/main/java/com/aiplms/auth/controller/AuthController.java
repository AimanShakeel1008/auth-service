package com.aiplms.auth.controller;

import com.aiplms.auth.dto.v1.ApiResponse;
import com.aiplms.auth.dto.v1.RegisterRequestDto;
import com.aiplms.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequestDto request) {
        Map<String, Object> data = authService.register(request);
        ApiResponse<Map<String, Object>> body = new ApiResponse<>("AUTH_001", "User registered successfully", data);
        return ResponseEntity.ok(body);
    }
}

