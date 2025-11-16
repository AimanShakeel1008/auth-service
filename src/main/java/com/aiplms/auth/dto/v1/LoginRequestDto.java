package com.aiplms.auth.dto.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Login request: accept username or email + password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank(message = ValidationMessages.REQUIRED)
    private String usernameOrEmail;

    @NotBlank(message = ValidationMessages.REQUIRED)
    @Size(min = 8, max = 128, message = ValidationMessages.PASSWORD_SIZE)
    private String password;
}

