package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Email verification token DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationRequestDto {

    @NotBlank(message = ValidationMessages.TOKEN_REQUIRED)
    private String token;
}

