package com.aiplms.auth.service.impl;

import com.aiplms.auth.entity.RefreshToken;
import com.aiplms.auth.entity.User;
import com.aiplms.auth.repository.RefreshTokenRepository;
import com.aiplms.auth.service.RefreshTokenService;
import com.aiplms.auth.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public CreateResult createForUser(User user, Instant expiresAt) {
        // generate opaque token for client
        String plainToken = TokenUtil.generateOpaqueToken();
        // hash for storage
        String tokenHash = TokenUtil.sha256Hex(plainToken);

        RefreshToken rt = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(tokenHash)
                .revoked(false)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .build();

        RefreshToken saved = refreshTokenRepository.save(rt);

        return new CreateResult(plainToken, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<RefreshToken> findByHash(String tokenHash) {
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }

    @Override
    @Transactional
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
}
