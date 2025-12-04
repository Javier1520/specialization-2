package com.epam.gym.security;

import com.epam.gym.exception.AccountLockedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 5;

    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    public void checkAccountLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt != null && attempt.isLocked()) {
            long remainingMinutes = attempt.getRemainingLockoutMinutes();
            log.warn("Account locked for username: {}. Remaining lockout time: {} minutes", username, remainingMinutes);
            throw new AccountLockedException("Account is locked due to too many failed login attempts. Please try again in " + remainingMinutes + " minutes.");
        }
    }

    public void recordFailedAttempt(String username) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(username, k -> new LoginAttempt());
        attempt.incrementFailedAttempts();

        if (attempt.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            attempt.lock();
            log.warn("Account locked for username: {} after {} failed attempts", username, attempt.getFailedAttempts());
        } else {
            log.debug("Failed login attempt for username: {}. Attempts: {}", username, attempt.getFailedAttempts());
        }
    }

    public void resetFailedAttempts(String username) {
        loginAttempts.remove(username);
        log.debug("Reset failed login attempts for username: {}", username);
    }

    private static class LoginAttempt {
        private int failedAttempts = 0;
        private LocalDateTime lockoutTime;

        public void incrementFailedAttempts() {
            this.failedAttempts++;
        }

        public int getFailedAttempts() {
            return failedAttempts;
        }

        public void lock() {
            this.lockoutTime = LocalDateTime.now();
        }

        public boolean isLocked() {
            if (lockoutTime == null) {
                return false;
            }

            LocalDateTime unlockTime = lockoutTime.plusMinutes(LOCKOUT_DURATION_MINUTES);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                return true;
            }

            // Lockout period expired, reset
            this.failedAttempts = 0;
            this.lockoutTime = null;
            return false;
        }

        public long getRemainingLockoutMinutes() {
            if (lockoutTime == null) {
                return 0;
            }
            LocalDateTime unlockTime = lockoutTime.plusMinutes(LOCKOUT_DURATION_MINUTES);
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(unlockTime)) {
                return java.time.Duration.between(now, unlockTime).toMinutes() + 1;
            }
            return 0;
        }
    }
}

