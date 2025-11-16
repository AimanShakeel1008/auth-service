package com.aiplms.auth.dto.v1;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

/**
 * Public projection of a user returned to the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private Instant createdAt;
}

