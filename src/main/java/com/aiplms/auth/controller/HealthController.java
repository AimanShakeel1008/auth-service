package com.aiplms.auth.controller;

import com.aiplms.auth.dto.v1.ApiResponse;
import com.aiplms.auth.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example controller to demonstrate uniform success response usage.
 * (Not part of API surface; remove or keep as a sample.)
 */
@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ResponseEntity<ApiResponse<Object>> health() {
        ApiResponse<Object> resp = ResponseUtil.success("AUTH_000", "Service healthy");
        return ResponseEntity.ok(resp);
    }
}


