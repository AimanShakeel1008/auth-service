package com.aiplms.auth.service;

import com.aiplms.auth.dto.v1.RegisterRequestDto;
import com.aiplms.auth.entity.Role;
import com.aiplms.auth.entity.User;
import com.aiplms.auth.exception.Exceptions;
import com.aiplms.auth.repository.RoleRepository;
import com.aiplms.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Map<String, Object> register(RegisterRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();

        // password confirm
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            // use existing Exceptions factory to produce a BaseException handled by GlobalExceptionHandler
            throw Exceptions.badRequest("Password and passwordConfirm do not match");
        }

        // uniqueness checks: rely on repository helper methods
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            // keep message generic, but include detail for logs if needed
            throw Exceptions.userAlreadyExists("email or username already exists");
        }


        // Map DTO -> entity (set only fields we know exist)
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .orElseThrow(() -> Exceptions.internal("Default role ROLE_USER not present"));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);

        // try to set createdAt if field exists in entity
        try {
            user.setCreatedAt(OffsetDateTime.now().toInstant());
        } catch (Throwable ignored) {}

        User saved = userRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("username", saved.getUsername());
        data.put("email", saved.getEmail());
        return data;
    }
}

