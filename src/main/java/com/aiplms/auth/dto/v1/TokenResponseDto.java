package com.aiplms.auth.dto.v1;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response containing new access + refresh tokens and expiry metadata.
 */
@Getter
@AllArgsConstructor
public class TokenResponseDto {
    private final String accessToken;
    private final String accessTokenExpiresAt;
    private final String refreshToken;
    private final String refreshTokenExpiresAt;
}
