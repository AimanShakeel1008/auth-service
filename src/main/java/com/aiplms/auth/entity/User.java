package com.aiplms.auth.entity;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class User {
    private Long id;
    private String username;
    private String email;
    private String passwordHash; // store hashed password; field name avoids accidental serialization of raw password
    private Instant createdAt;
    private boolean emailVerified;
}

