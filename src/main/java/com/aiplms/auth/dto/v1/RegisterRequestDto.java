package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Request DTO for user registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDto {

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Size(min = 3, max = 50, message = ValidationMessages.USERNAME_SIZE)
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "Username contains invalid characters")
    private String username;

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Email(message = ValidationMessages.INVALID_EMAIL)
    private String email;

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Size(min = 8, max = 128, message = ValidationMessages.PASSWORD_SIZE)
    private String password;

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Size(min = 8, max = 128, message = ValidationMessages.PASSWORD_SIZE)
    private String passwordConfirm;
}

