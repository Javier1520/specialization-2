package com.epam.gym.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private static final String TEST_SECRET =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
  private static final long TEST_EXPIRATION = 3600000; // 1 hour
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
    ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);
  }

  @Test
  void generateToken_validUsername_shouldReturnToken() {
    // Given
    String username = "testuser";

    // When
    String token = jwtService.generateToken(username);

    // Then
    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void extractUsername_validToken_shouldReturnUsername() {
    // Given
    String username = "testuser";
    String token = jwtService.generateToken(username);

    // When
    String extractedUsername = jwtService.extractUsername(token);

    // Then
    assertEquals(username, extractedUsername);
  }

  @Test
  void extractExpiration_validToken_shouldReturnFutureDate() {
    // Given
    String username = "testuser";
    String token = jwtService.generateToken(username);

    // When
    Date expiration = jwtService.extractExpiration(token);

    // Then
    assertNotNull(expiration);
    assertTrue(expiration.after(new Date()));
  }

  @Test
  void validateToken_validTokenAndUsername_shouldReturnTrue() {
    // Given
    String username = "testuser";
    String token = jwtService.generateToken(username);

    // When
    Boolean isValid = jwtService.validateToken(token, username);

    // Then
    assertTrue(isValid);
  }

  @Test
  void validateToken_validTokenWrongUsername_shouldReturnFalse() {
    // Given
    String username = "testuser";
    String wrongUsername = "wronguser";
    String token = jwtService.generateToken(username);

    // When
    Boolean isValid = jwtService.validateToken(token, wrongUsername);

    // Then
    assertFalse(isValid);
  }

  @Test
  void validateToken_expiredToken_shouldThrowException() {
    // Given - Create service with very short expiration
    JwtService shortExpirationService = new JwtService();
    ReflectionTestUtils.setField(shortExpirationService, "secretKey", TEST_SECRET);
    ReflectionTestUtils.setField(shortExpirationService, "expiration", -1000L); // Already expired

    String username = "testuser";
    String token = shortExpirationService.generateToken(username);

    // When/Then - Expect exception when extracting from expired token
    assertThrows(
        io.jsonwebtoken.ExpiredJwtException.class,
        () -> shortExpirationService.extractUsername(token));
  }

  @Test
  void extractUsername_invalidToken_shouldThrowException() {
    // Given
    String invalidToken = "invalid.token.here";

    // When/Then
    assertThrows(JwtException.class, () -> jwtService.extractUsername(invalidToken));
  }

  @Test
  void generateToken_differentUsernames_shouldGenerateDifferentTokens() {
    // Given
    String user1 = "user1";
    String user2 = "user2";

    // When
    String token1 = jwtService.generateToken(user1);
    String token2 = jwtService.generateToken(user2);

    // Then
    assertNotEquals(token1, token2);
  }

  @Test
  void generateToken_sameUsernameTwice_shouldGenerateDifferentTokens() {
    // Given
    String username = "testuser";

    // When
    String token1 = jwtService.generateToken(username);
    // Small delay to ensure different issuedAt time
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    String token2 = jwtService.generateToken(username);

    // Then
    assertNotEquals(token1, token2);
  }

  @Test
  void extractClaim_extractSubject_shouldReturnUsername() {
    // Given
    String username = "testuser";
    String token = jwtService.generateToken(username);

    // When
    String subject = jwtService.extractClaim(token, claims -> claims.getSubject());

    // Then
    assertEquals(username, subject);
  }

  @Test
  void extractClaim_extractIssuedAt_shouldReturnRecentDate() {
    // Given
    String username = "testuser";
    Date beforeGeneration = new Date();
    String token = jwtService.generateToken(username);
    Date afterGeneration = new Date();

    // When
    Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());

    // Then
    assertNotNull(issuedAt);
    // Allow 1 second tolerance for test execution time
    assertTrue(issuedAt.getTime() >= beforeGeneration.getTime() - 1000);
    assertTrue(issuedAt.getTime() <= afterGeneration.getTime() + 1000);
  }
}
