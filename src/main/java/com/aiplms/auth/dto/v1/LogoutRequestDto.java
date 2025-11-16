package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Logout request: optional refresh token is provided to revoke it server-side.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutRequestDto {

    @NotBlank(message = ValidationMessages.TOKEN_REQUIRED)
    private String refreshToken;
}

