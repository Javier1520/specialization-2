package com.epam.gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.RefreshToken;
import com.epam.gym.repository.RefreshTokenRepository;
import com.epam.gym.service.impl.RefreshTokenServiceImpl;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

  private static final Long TEST_EXPIRATION = 86400000L; // 24 hours
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @InjectMocks private RefreshTokenServiceImpl refreshTokenService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", TEST_EXPIRATION);
  }

  @Test
  void createRefreshToken_validUsername_shouldCreateAndSaveToken() {
    // Given
    String username = "testuser";
    RefreshToken savedToken =
        RefreshToken.builder()
            .id(1L)
            .username(username)
            .token("generated-uuid")
            .expiryDate(Instant.now().plusMillis(TEST_EXPIRATION))
            .build();

    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

    // When
    RefreshToken result = refreshTokenService.createRefreshToken(username);

    // Then
    assertNotNull(result);
    assertEquals(username, result.getUsername());
    assertNotNull(result.getToken());

    ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(tokenCaptor.capture());

    RefreshToken capturedToken = tokenCaptor.getValue();
    assertEquals(username, capturedToken.getUsername());
    assertNotNull(capturedToken.getToken());
    assertNotNull(capturedToken.getExpiryDate());
    assertTrue(capturedToken.getExpiryDate().isAfter(Instant.now()));
  }

  @Test
  void createRefreshToken_shouldGenerateUniqueToken() {
    // Given
    String username = "testuser";
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    RefreshToken token1 = refreshTokenService.createRefreshToken(username);
    RefreshToken token2 = refreshTokenService.createRefreshToken(username);

    // Then
    assertNotEquals(token1.getToken(), token2.getToken());
  }

  @Test
  void createRefreshToken_shouldSetCorrectExpirationTime() {
    // Given
    String username = "testuser";
    Instant beforeCreation = Instant.now();

    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    RefreshToken result = refreshTokenService.createRefreshToken(username);

    // Then
    Instant expectedExpiry = beforeCreation.plusMillis(TEST_EXPIRATION);
    assertTrue(result.getExpiryDate().isAfter(expectedExpiry.minusSeconds(1)));
    assertTrue(result.getExpiryDate().isBefore(expectedExpiry.plusSeconds(1)));
  }

  @Test
  void findByToken_tokenExists_shouldReturnToken() {
    // Given
    String tokenString = "test-token-123";
    RefreshToken expectedToken =
        RefreshToken.builder()
            .id(1L)
            .username("testuser")
            .token(tokenString)
            .expiryDate(Instant.now().plusMillis(TEST_EXPIRATION))
            .build();

    when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(expectedToken));

    // When
    Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

    // Then
    assertTrue(result.isPresent());
    assertEquals(expectedToken, result.get());
    assertEquals(tokenString, result.get().getToken());
    verify(refreshTokenRepository).findByToken(tokenString);
  }

  @Test
  void findByToken_tokenNotFound_shouldReturnEmpty() {
    // Given
    String tokenString = "nonexistent-token";
    when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());

    // When
    Optional<RefreshToken> result = refreshTokenService.findByToken(tokenString);

    // Then
    assertFalse(result.isPresent());
    verify(refreshTokenRepository).findByToken(tokenString);
  }

  @Test
  void verifyExpiration_validToken_shouldReturnToken() {
    // Given
    RefreshToken validToken =
        RefreshToken.builder()
            .id(1L)
            .username("testuser")
            .token("valid-token")
            .expiryDate(Instant.now().plusMillis(TEST_EXPIRATION))
            .build();

    // When
    RefreshToken result = refreshTokenService.verifyExpiration(validToken);

    // Then
    assertNotNull(result);
    assertEquals(validToken, result);
    verify(refreshTokenRepository, never()).delete(any());
  }

  @Test
  void verifyExpiration_expiredToken_shouldDeleteAndThrowException() {
    // Given
    RefreshToken expiredToken =
        RefreshToken.builder()
            .id(1L)
            .username("testuser")
            .token("expired-token")
            .expiryDate(Instant.now().minusMillis(1000)) // Expired 1 second ago
            .build();

    // When/Then
    ValidationException exception =
        assertThrows(
            ValidationException.class, () -> refreshTokenService.verifyExpiration(expiredToken));

    assertTrue(exception.getMessage().contains("Refresh token was expired"));
    verify(refreshTokenRepository).delete(expiredToken);
  }

  @Test
  void verifyExpiration_tokenExpiringNow_shouldDeleteAndThrowException() {
    // Given
    RefreshToken expiringToken =
        RefreshToken.builder()
            .id(1L)
            .username("testuser")
            .token("expiring-token")
            .expiryDate(Instant.now().minusMillis(1)) // Just expired
            .build();

    // When/Then
    assertThrows(
        ValidationException.class, () -> refreshTokenService.verifyExpiration(expiringToken));

    verify(refreshTokenRepository).delete(expiringToken);
  }

  @Test
  void deleteByToken_existingToken_shouldDeleteToken() {
    // Given
    String tokenString = "token-to-delete";

    // When
    refreshTokenService.deleteByToken(tokenString);

    // Then
    verify(refreshTokenRepository).deleteByToken(tokenString);
  }

  @Test
  void deleteByToken_nonexistentToken_shouldStillCallRepository() {
    // Given
    String tokenString = "nonexistent-token";

    // When
    refreshTokenService.deleteByToken(tokenString);

    // Then
    verify(refreshTokenRepository).deleteByToken(tokenString);
  }

  @Test
  void createRefreshToken_withCustomExpiration_shouldUseCustomValue() {
    // Given
    Long customExpiration = 3600000L; // 1 hour
    ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", customExpiration);
    String username = "testuser";

    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    RefreshToken result = refreshTokenService.createRefreshToken(username);

    // Then
    Instant expectedExpiry = Instant.now().plusMillis(customExpiration);
    assertTrue(result.getExpiryDate().isBefore(expectedExpiry.plusSeconds(1)));
  }

  @Test
  void verifyExpiration_exceptionMessage_shouldBeDescriptive() {
    // Given
    RefreshToken expiredToken =
        RefreshToken.builder()
            .id(1L)
            .username("testuser")
            .token("expired-token")
            .expiryDate(Instant.now().minusMillis(1000))
            .build();

    // When
    ValidationException exception =
        assertThrows(
            ValidationException.class, () -> refreshTokenService.verifyExpiration(expiredToken));

    // Then
    assertTrue(exception.getMessage().contains("Refresh token was expired"));
    assertTrue(exception.getMessage().contains("signin"));
  }
}
