package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Request to start password reset (send email).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetRequestDto {

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Email(message = ValidationMessages.INVALID_EMAIL)
    private String email;
}
