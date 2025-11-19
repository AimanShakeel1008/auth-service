package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request payload for refreshing tokens.
 */
@Getter
@Setter
@NoArgsConstructor
public class RefreshRequestDto {

    @NotBlank(message = ValidationMessages.REQUIRED)
    private String refreshToken;
}
