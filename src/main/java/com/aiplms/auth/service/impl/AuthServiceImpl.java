package com.aiplms.auth.service.impl;

import com.aiplms.auth.config.AuthProperties;
import com.aiplms.auth.dto.v1.LoginRequestDto;
import com.aiplms.auth.dto.v1.RegisterRequestDto;
import com.aiplms.auth.entity.Role;
import com.aiplms.auth.entity.User;
import com.aiplms.auth.exception.Exceptions;
import com.aiplms.auth.repository.RoleRepository;
import com.aiplms.auth.repository.UserRepository;
import com.aiplms.auth.security.JwtService;
import com.aiplms.auth.service.AuthService;
import com.aiplms.auth.service.EmailVerificationService;
import com.aiplms.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthProperties authProperties;
    private final EmailVerificationService emailVerificationService;

    @Override
    @Transactional
    public Map<String, Object> register(RegisterRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();

        // password confirm
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            // use existing Exceptions factory to produce a BaseException handled by GlobalExceptionHandler
            throw Exceptions.badRequest("Password and passwordConfirm do not match");
        }

        // uniqueness checks: rely on repository helper methods
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            // keep message generic, but include detail for logs if needed
            throw Exceptions.userAlreadyExists("email or username already exists");
        }


        // Map DTO -> entity (set only fields we know exist)
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .orElseThrow(() -> Exceptions.internal("Default role ROLE_USER not present"));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);

        // try to set createdAt if field exists in entity
        try {
            user.setCreatedAt(OffsetDateTime.now().toInstant());
        } catch (Throwable ignored) {}

        User saved = userRepository.save(user);

        // create verification token & send email
        String verificationToken = emailVerificationService.createAndSendToken(saved);

        Map<String, Object> data = new HashMap<>();

        data.put("id", saved.getId());
        data.put("username", saved.getUsername());
        data.put("email", saved.getEmail());
        data.put("message", "Verification email sent. Please check your inbox.");

        JwtService.AccessToken accessToken = jwtService.createAccessToken(user);
        data.put("accessToken", accessToken.getToken());
        data.put("accessTokenExpiresAt", accessToken.getExpiresAtIso());

        Instant refreshExpiry = Instant.now().plus(authProperties.getRefreshTokenTtl());
        RefreshTokenService.CreateResult rtResult = refreshTokenService.createForUser(user, refreshExpiry);
        data.put("refreshToken", rtResult.getPlainToken());
        data.put("refreshTokenExpiresAt", refreshExpiry.toString());

        return data;
    }

    @Override
    public Map<String, Object> login(LoginRequestDto request) {
        // find by username or email
        User user = userRepository.findByUsernameIgnoreCase(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmailIgnoreCase(request.getUsernameOrEmail()))
                .orElseThrow(() -> Exceptions.unauthorized("Invalid credentials"));

        // check enabled
        if (!Boolean.TRUE.equals(user.isEnabled())) {
            throw Exceptions.unauthorized("User account is disabled");
        }

        if (Boolean.FALSE.equals(user.isEmailVerified())) {
            // uniform error format via Exceptions helper
            throw Exceptions.unauthorized("Email not verified. Please check your inbox or request a resend.");
        }

        // Check if account is locked
        Instant lockedUntil = user.getLockedUntil();
        if (lockedUntil != null && lockedUntil.isAfter(Instant.now())) {
            String msg = String.format("Account is locked until %s", lockedUntil.toString());
            throw Exceptions.accountLocked(msg);
        }

        // password verification
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            // increment failed login count and persist
            int failed = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failed);

            // if threshold reached -> lock account
            if (failed >= authProperties.getMaxFailedAttempts()) {
                Instant until = Instant.now().plus(authProperties.getLockoutDuration());
                user.setLockedUntil(until);
                user.setFailedLoginCount(0); // reset counter after locking
                userRepository.save(user);

                String msg = String.format("Account locked due to repeated failed login attempts. Locked until: %s", until.toString());
                throw Exceptions.accountLocked(msg);
            } else {
                userRepository.save(user);
                throw Exceptions.unauthorized("Invalid credentials");
            }
        }

        // Successful login: reset failed count and lockedUntil if set
        if (user.getFailedLoginCount() != 0 || user.getLockedUntil() != null) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());

        JwtService.AccessToken accessToken = jwtService.createAccessToken(user);
        data.put("accessToken", accessToken.getToken());
        data.put("accessTokenExpiresAt", accessToken.getExpiresAtIso());

        Instant refreshExpiry = Instant.now().plus(30, ChronoUnit.DAYS); // example: 30 days
        RefreshTokenService.CreateResult rtResult = refreshTokenService.createForUser(user, refreshExpiry);
        data.put("refreshToken", rtResult.getPlainToken());
        data.put("refreshTokenExpiresAt", refreshExpiry.toString());

        return data;
    }

}
