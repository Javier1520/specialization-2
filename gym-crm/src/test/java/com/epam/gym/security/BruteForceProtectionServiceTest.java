package com.epam.gym.security;

import com.epam.gym.exception.AccountLockedException;
import com.epam.gym.util.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionServiceTest {

    @Mock private LogUtils logUtils;

    private BruteForceProtectionService bruteForceProtectionService;

    @BeforeEach
    void setUp() {
        bruteForceProtectionService = new BruteForceProtectionService(logUtils);
    }

    @Test
    void checkAccountLocked_whenNoAttempts_shouldNotThrow() {
        // Given
        String username = "testuser";

        // When/Then
        assertDoesNotThrow(() -> bruteForceProtectionService.checkAccountLocked(username));
    }

    @Test
    void recordFailedAttempt_firstAttempt_shouldNotLockAccount() {
        // Given
        String username = "testuser";

        // When
        bruteForceProtectionService.recordFailedAttempt(username);

        // Then
        assertDoesNotThrow(() -> bruteForceProtectionService.checkAccountLocked(username));
    }

    @Test
    void recordFailedAttempt_secondAttempt_shouldNotLockAccount() {
        // Given
        String username = "testuser";

        // When
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);

        // Then
        assertDoesNotThrow(() -> bruteForceProtectionService.checkAccountLocked(username));
    }

    @Test
    void recordFailedAttempt_threeAttempts_shouldLockAccount() {
        // Given
        String username = "testuser";

        // When
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);

        // Then
        AccountLockedException exception =
                assertThrows(
                        AccountLockedException.class,
                        () -> bruteForceProtectionService.checkAccountLocked(username));
        assertTrue(exception.getMessage().contains("Account is locked"));
        assertTrue(exception.getMessage().contains("minutes"));
    }

    @Test
    void recordFailedAttempt_moreThanThreeAttempts_shouldStayLocked() {
        // Given
        String username = "testuser";

        // When
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username); // 4th attempt

        // Then
        assertThrows(
                AccountLockedException.class,
                () -> bruteForceProtectionService.checkAccountLocked(username));
    }

    @Test
    void resetFailedAttempts_afterFailedAttempts_shouldUnlockAccount() {
        // Given
        String username = "testuser";
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);

        // When
        bruteForceProtectionService.resetFailedAttempts(username);

        // Then
        assertDoesNotThrow(() -> bruteForceProtectionService.checkAccountLocked(username));
    }

    @Test
    void resetFailedAttempts_afterAccountLocked_shouldUnlockAccount() {
        // Given
        String username = "testuser";
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);

        assertThrows(
                AccountLockedException.class,
                () -> bruteForceProtectionService.checkAccountLocked(username));

        // When
        bruteForceProtectionService.resetFailedAttempts(username);

        // Then
        assertDoesNotThrow(() -> bruteForceProtectionService.checkAccountLocked(username));
    }

    @Test
    void checkAccountLocked_multipleDifferentUsers_shouldBeIndependent() {
        // Given
        String user1 = "user1";
        String user2 = "user2";

        // When
        bruteForceProtectionService.recordFailedAttempt(user1);
        bruteForceProtectionService.recordFailedAttempt(user1);
        bruteForceProtectionService.recordFailedAttempt(user1);

        // Then
        assertThrows(
                AccountLockedException.class,
                () -> bruteForceProtectionService.checkAccountLocked(user1));
        assertDoesNotThrow(() -> bruteForceProtectionService.checkAccountLocked(user2));
    }

    @Test
    void resetFailedAttempts_whenNoAttempts_shouldNotThrow() {
        // Given
        String username = "testuser";

        // When/Then
        assertDoesNotThrow(() -> bruteForceProtectionService.resetFailedAttempts(username));
    }

    @Test
    void checkAccountLocked_exceptionMessage_shouldIncludeUsername() {
        // Given
        String username = "testuser";
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);
        bruteForceProtectionService.recordFailedAttempt(username);

        // When
        AccountLockedException exception =
                assertThrows(
                        AccountLockedException.class,
                        () -> bruteForceProtectionService.checkAccountLocked(username));

        // Then
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("too many failed login attempts"));
    }
}
