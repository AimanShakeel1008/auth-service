package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Refresh token request: client sends opaque refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshRequestDto {

    @NotBlank(message = ValidationMessages.TOKEN_REQUIRED)
    private String refreshToken;
}

