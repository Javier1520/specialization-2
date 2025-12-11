package com.epam.gym.service.impl;

import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.RefreshToken;
import com.epam.gym.repository.RefreshTokenRepository;
import com.epam.gym.service.RefreshTokenService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration:86400000}") // Default 24 hours
    private Long refreshTokenDurationMs;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .username(username)
                        .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                        .token(UUID.randomUUID().toString())
                        .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (isTokenExpired(token)) {
            deleteExpiredToken(token);
            throw new ValidationException(
                    "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    private boolean isTokenExpired(RefreshToken token) {
        return token.getExpiryDate().compareTo(Instant.now()) < 0;
    }

    private void deleteExpiredToken(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
