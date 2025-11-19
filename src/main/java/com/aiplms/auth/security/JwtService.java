package com.aiplms.auth.security;

import com.aiplms.auth.entity.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * Minimal JWT helper for HS256 usage (Step 9/10).
 *
 * Produces tokens (createAccessToken) and validates/parses them (parseAndValidate).
 *
 * IMPORTANT: In later steps you'll switch to RS256 and JWKS. This implementation is deliberately minimal
 * for Step-9/10's HS256 approach.
 */
@Slf4j
@Component
public class JwtService {

    @Value("${auth.jwt.secret}")
    private String sharedSecret;

    @Value("${auth.jwt.access-token-ttl-seconds:900}")
    private long accessTokenTtlSeconds;

    private static final DateTimeFormatter ISO_FORMAT =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    @Getter
    public static class AccessToken {
        private final String token;
        private final String expiresAtIso;

        public AccessToken(String token, Instant expiresAt) {
            this.token = token;
            this.expiresAtIso = ISO_FORMAT.format(expiresAt);
        }

        public String getToken() {
            return token;
        }

        public String getExpiresAtIso() {
            return expiresAtIso;
        }
    }

    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String msg) { super(msg); }
        public JwtValidationException(String msg, Throwable cause) { super(msg, cause); }
    }

    /**
     * Create an HS256 signed JWT for the given user.
     */
    public AccessToken createAccessToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(accessTokenTtlSeconds);

            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .subject(user.getId().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim("id", user.getId().toString())
                    .claim("username", user.getUsername())
                    .claim("email", user.getEmail());

            JWTClaimsSet claimsSet = claims.build();

            com.nimbusds.jose.JWSSigner signer = new MACSigner(sharedSecret.getBytes(StandardCharsets.UTF_8));
            com.nimbusds.jose.JWSHeader header = new com.nimbusds.jose.JWSHeader(com.nimbusds.jose.JWSAlgorithm.HS256);

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);

            return new AccessToken(signedJWT.serialize(), expiresAt);
        } catch (JOSEException e) {
            log.error("Failed to create JWT access token", e);
            throw new RuntimeException("Failed to create JWT access token", e);
        }
    }

    /**
     * Parse and validate token, returning a wrapper with easy access to claims.
     * Throws JwtValidationException on any problem (signature, expiry, parse error).
     */
    public JwtClaims parseAndValidate(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(sharedSecret.getBytes(StandardCharsets.UTF_8));
            boolean verified = signedJWT.verify(verifier);
            if (!verified) {
                throw new JwtValidationException("Invalid JWT signature");
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            Date exp = claims.getExpirationTime();
            if (exp == null) {
                throw new JwtValidationException("Missing exp claim");
            }
            if (exp.before(new Date())) {
                throw new JwtValidationException("JWT token expired");
            }

            return new JwtClaims(claims);
        } catch (ParseException | JOSEException e) {
            throw new JwtValidationException("Failed to parse/validate JWT", e);
        }
    }

    /**
     * Lightweight wrapper around JWTClaimsSet for simpler use.
     */
    public static class JwtClaims {
        private final JWTClaimsSet inner;

        JwtClaims(JWTClaimsSet inner) {
            this.inner = inner;
        }

        public String getClaimAsString(String name) {
            Object v = inner.getClaim(name);
            return v == null ? null : v.toString();
        }

        public Map<String, Object> getAllClaims() {
            return inner.getClaims();
        }

        public String getSubject() {
            return inner.getSubject();
        }
    }
}
