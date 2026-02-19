package com.epam.gym.workload.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    }

    private String generateTestToken(String username, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .id(UUID.randomUUID().toString())
                .signWith(key)
                .compact();
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String username = "testuser";
        String token = generateTestToken(username, 3600000); // 1 hour

        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        String username = "testuser";
        String token = generateTestToken(username, 3600000);

        assertTrue(jwtService.validateToken(token, username));
    }

    @Test
    void validateToken_shouldReturnFalse_whenUsernameDoesNotMatch() {
        String username = "testuser";
        String token = generateTestToken(username, 3600000);

        assertFalse(jwtService.validateToken(token, "otheruser"));
    }

    @Test
    void validateToken_shouldThrowException_whenTokenIsExpired() {
        String username = "testuser";
        String token = generateTestToken(username, -1000); // Expired

        assertThrows(JwtException.class, () -> jwtService.validateToken(token, username));
    }

    @Test
    void extractExpiration_shouldReturnCorrectExpiration() {
        String token = generateTestToken("testuser", 3600000);
        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }
}
