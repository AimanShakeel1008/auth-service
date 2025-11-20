package com.aiplms.auth.controller;

import com.aiplms.auth.config.AuthProperties;
import com.aiplms.auth.dto.v1.*;
import com.aiplms.auth.entity.RefreshToken;
import com.aiplms.auth.exception.Exceptions;
import com.aiplms.auth.mapper.UserMapper;
import com.aiplms.auth.repository.UserRepository;
import com.aiplms.auth.security.JwtService;
import com.aiplms.auth.service.AuthService;
import com.aiplms.auth.service.RefreshTokenService;
import com.aiplms.auth.service.TokenBlacklistService;
import com.aiplms.auth.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final TokenBlacklistService tokenBlacklistService;


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

    /**
     * Refresh endpoint with rotation.
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDto>> refreshToken(
            @Valid @RequestBody RefreshRequestDto request
    ) {
        String incoming = request.getRefreshToken();
        if (incoming == null || incoming.isBlank()) {
            throw Exceptions.badRequest("Missing refresh token");
        }

        // compute hash using TokenUtil (existing util)
        String hash = TokenUtil.sha256Hex(incoming);

        // find persisted refresh token by hash
        var opt = refreshTokenService.findByHash(hash);
        if (opt.isEmpty()) {
            // token not found -> invalid or reuse
            throw Exceptions.unauthorized("Invalid refresh token");
        }

        RefreshToken stored = opt.get();

        // check revoked / expired
        if (stored.isRevoked()) {
            // possible reuse / replay — return unauthorized
            // TODO: log details for audit; optionally revoke all tokens for user
            throw Exceptions.unauthorized("Refresh token has been revoked");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            // expired
            throw Exceptions.unauthorized("Refresh token expired");
        }

        // rotation: revoke current token
        refreshTokenService.revoke(stored);

        // create new refresh token (expiry using authProperties or a TTL; AuthServiceImpl uses authProperties)
        Instant newExpiry = Instant.now().plus(authProperties.getRefreshTokenTtl());
        var createResult = refreshTokenService.createForUser(stored.getUser(), newExpiry);

        // create new access token
        JwtService.AccessToken accessToken = jwtService.createAccessToken(stored.getUser());

        TokenResponseDto resp = new TokenResponseDto(
                accessToken.getToken(),
                accessToken.getExpiresAtIso(),
                createResult.getPlainToken(),
                newExpiry.toString()
        );

        var body = new ApiResponse<>("AUTH_013", "Token refreshed", resp);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @Valid @RequestBody LogoutRequestDto request,
            HttpServletRequest httpRequest,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        // 1) Blacklist access token from Authorization header (if present)
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7).trim();
            try {
                // delegate to blacklist service
                tokenBlacklistService.blacklistAccessToken(accessToken);
            } catch (Exception ex) {
                // do not fail logout if blacklist fails; log and continue (but surface mild error if you want)
                log.warn("Failed to blacklist access token during logout", ex);
            }
        }

        // 2) Revoke refresh token if provided
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            String providedRefresh = request.getRefreshToken();
            String hash = TokenUtil.sha256Hex(providedRefresh);
            java.util.Optional<RefreshToken> refreshOpt = refreshTokenService.findByHash(hash);
            if (refreshOpt.isPresent()) {
                // revoke via refresh token service
                refreshTokenService.revoke(refreshOpt.get());
            } else {
                // no-op if not found
                log.debug("Logout requested for unknown refresh token (hash={})", hash);
            }
        }

        // 3) Clear Spring Security context for safety
        SecurityContextHolder.clearContext();

        var body = new ApiResponse<>("AUTH_014", "Logged out", null);
        return ResponseEntity.ok(body);
    }
}

