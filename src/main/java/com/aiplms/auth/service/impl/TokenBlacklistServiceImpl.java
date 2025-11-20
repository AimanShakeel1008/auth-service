package com.aiplms.auth.service.impl;

import com.aiplms.auth.service.TokenBlacklistService;
import com.aiplms.auth.security.JwtService;
import com.aiplms.auth.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementation stores a Redis key per token with TTL set to remaining token lifetime.
 * Key pattern: auth:blacklist:access:{jtiOrHash}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    private static final String KEY_PREFIX = "auth:blacklist:access:";

    @Override
    public void blacklistAccessToken(String jwt) {
        if (jwt == null || jwt.isBlank()) {
            throw new IllegalArgumentException("jwt must be provided");
        }

        JwtService.JwtClaims claims = jwtService.parseAndValidate(jwt);
        // Try to get jti
        String jti = claims.getClaimAsString("jti");
        String keyId;
        if (jti != null && !jti.isBlank()) {
            keyId = jti;
        } else {
            keyId = TokenUtil.sha256Hex(jwt);
        }

        long ttlSeconds = computeTtlSecondsFromClaims(claims.getAllClaims());
        if (ttlSeconds <= 0) {
            // If token already expired or TTL not determined, use a conservative TTL (e.g. 1 minute).
            ttlSeconds = 60;
        }

        String redisKey = KEY_PREFIX + keyId;
        redisTemplate.opsForValue().set(redisKey, "1", ttlSeconds, TimeUnit.SECONDS);
        log.info("Blacklisted access token key={} ttlSeconds={}", redisKey, ttlSeconds);
    }

    @Override
    public boolean isBlacklisted(String jwt) {
        if (jwt == null || jwt.isBlank()) return false;
        try {
            JwtService.JwtClaims claims = jwtService.parseAndValidate(jwt);
            String jti = claims.getClaimAsString("jti");
            String keyId = (jti != null && !jti.isBlank()) ? jti : TokenUtil.sha256Hex(jwt);
            String redisKey = KEY_PREFIX + keyId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception ex) {
            // If token invalid, consider it 'not blacklisted' here - caller should have validated token earlier.
            return false;
        }
    }

    /**
     * TTL calculation: extract 'exp' claim (expected numeric seconds or java.util.Date)
     */
    private long computeTtlSecondsFromClaims(Map<String, Object> claims) {
        if (claims == null) return -1L;
        Object expObj = claims.get("exp");
        if (expObj == null) {
            // try nested types or string
            return -1L;
        }
        // If expObj is java.util.Date (some JWT libs map it), compute ttl
        if (expObj instanceof Date) {
            Date exp = (Date) expObj;
            long diff = (exp.getTime() - System.currentTimeMillis()) / 1000L;
            return diff;
        }
        // If Number (seconds since epoch)
        if (expObj instanceof Number) {
            long expSeconds = ((Number) expObj).longValue();
            long nowSeconds = System.currentTimeMillis() / 1000L;
            return expSeconds - nowSeconds;
        }
        // If string numeric
        try {
            long expSeconds = Long.parseLong(expObj.toString());
            long nowSeconds = System.currentTimeMillis() / 1000L;
            return expSeconds - nowSeconds;
        } catch (Exception e) {
            return -1L;
        }
    }
}
