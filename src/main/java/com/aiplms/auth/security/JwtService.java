package com.aiplms.auth.security;

import com.aiplms.auth.entity.User;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * JwtService: creates HS256 signed access tokens using Nimbus (10.0.2).
 *
 * Note: For production use a properly managed secret (>=32 bytes) and later migrate to RS256.
 */
@Component
@Slf4j
public class JwtService {

    private final byte[] sharedSecret;
    private final long accessTokenTtlSeconds;

    public JwtService(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.access-token-ttl-seconds}") long accessTokenTtlSeconds) {
        if (secret == null || secret.length() < 32) {
            log.warn("JWT secret length is less than 32 chars — ensure you override it in production!");
        }
        this.sharedSecret = secret.getBytes();
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public static class AccessToken {
        private final String token;
        private final Instant expiresAt;

        public AccessToken(String token, Instant expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }

        public String getToken() {
            return token;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public String getExpiresAtIso() {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC).format(expiresAt);
        }
    }

    /**
     * Create HS256 signed access token for given user.
     *
     * @param user user entity (must have id, username, email; roles optional)
     * @return AccessToken containing token string and expiry instant (UTC)
     */
    public AccessToken createAccessToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(accessTokenTtlSeconds);

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(user.getId()))
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim("preferred_username", user.getUsername())
                    .claim("email", user.getEmail());

            // Attach roles if available
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                List<String> roleNames = new ArrayList<>();
                user.getRoles().forEach(role -> {
                    if (role != null && role.getName() != null) {
                        roleNames.add(role.getName());
                    }
                });
                claimsBuilder.claim("roles", Collections.unmodifiableList(roleNames));
            }

            JWTClaimsSet claims = claimsBuilder.build();

            // Create the SignedJWT with HS256 header
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);

            // Sign with HMAC signer using shared secret bytes
            JWSSigner signer = new MACSigner(sharedSecret);
            signedJWT.sign(signer);

            String token = signedJWT.serialize();
            return new AccessToken(token, expiresAt);
        } catch (JOSEException e) {
            log.error("Failed to create JWT access token", e);
            throw new RuntimeException("Failed to create JWT access token", e);
        }
    }
}

