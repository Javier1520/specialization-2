package com.epam.gym.security;

import com.epam.gym.exception.AccountLockedException;
import com.epam.gym.util.LogUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BruteForceProtectionService {
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 5;
    private static final Logger log = LoggerFactory.getLogger(BruteForceProtectionService.class);

    private final LogUtils logUtils;
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    public BruteForceProtectionService(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    public void checkAccountLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (isAccountLocked(attempt)) {
            long remainingMinutes = attempt.getRemainingLockoutMinutes();
            logUtils.warn(
                    log,
                    "Account locked for username: {}. Remaining lockout time: {} minutes",
                    username,
                    remainingMinutes);
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts. Please try again in "
                            + remainingMinutes
                            + " minutes.");
        }
    }

    private boolean isAccountLocked(LoginAttempt attempt) {
        return attempt != null && attempt.isLocked();
    }

    public void recordFailedAttempt(String username) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(username, k -> new LoginAttempt());
        attempt.incrementFailedAttempts();

        if (hasExceededMaxFailedAttempts(attempt)) {
            attempt.lock();
            logUtils.warn(
                    log,
                    "Account locked for username: {} after {} failed attempts",
                    username,
                    attempt.getFailedAttempts());
            return;
        }

        logUtils.debug(
                log,
                "Failed login attempt for username: {}. Attempts: {}",
                username,
                attempt.getFailedAttempts());
    }

    private boolean hasExceededMaxFailedAttempts(LoginAttempt attempt) {
        return attempt.getFailedAttempts() >= MAX_FAILED_ATTEMPTS;
    }

    public void resetFailedAttempts(String username) {
        loginAttempts.remove(username);
        logUtils.debug(log, "Reset failed login attempts for username: {}", username);
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
            if (lockoutTime == null) return false;

            if (isLockoutPeriodExpired()) {
                resetFailedAttemptsAndLockoutTime();
                return false;
            }
            return true;
        }

        private boolean isLockoutPeriodExpired() {
            return LocalDateTime.now().isAfter(getUnlockTime());
        }

        private LocalDateTime getUnlockTime() {
            return lockoutTime.plusMinutes(LOCKOUT_DURATION_MINUTES);
        }

        private void resetFailedAttemptsAndLockoutTime() {
            this.failedAttempts = 0;
            this.lockoutTime = null;
        }


        public long getRemainingLockoutMinutes() {
            if (lockoutTime == null) return 0;
            LocalDateTime unlockTime = getUnlockTime();
            if (isLockoutPeriodActive())
                return Duration.between(LocalDateTime.now(), unlockTime).toMinutes() + 1;

            return 0;
        }

        private boolean isLockoutPeriodActive() {
            return LocalDateTime.now().isBefore(getUnlockTime());
        }
    }
}
