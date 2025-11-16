package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Confirm password reset using token + new password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetConfirmRequestDto {

    @NotBlank(message = ValidationMessages.TOKEN_REQUIRED)
    private String token;

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Size(min = 8, max = 128, message = ValidationMessages.PASSWORD_SIZE)
    private String newPassword;

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Size(min = 8, max = 128, message = ValidationMessages.PASSWORD_SIZE)
    private String newPasswordConfirm;
}

